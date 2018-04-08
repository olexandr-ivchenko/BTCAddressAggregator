package com.olexandrivchenko.bitcoinkiller.database;

import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCaller;
import com.olexandrivchenko.bitcoinkiller.database.main.AppExecutor;
import com.olexandrivchenko.bitcoinkiller.database.main.HelpProvider;
import com.olexandrivchenko.bitcoinkiller.database.main.ParametersParser;
import com.olexandrivchenko.bitcoinkiller.database.main.dto.CommandLineOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

@SpringBootApplication
public class BTCKillerDatabaseApp implements CommandLineRunner {

    @Value("${testconfigvalue:unknown}")
    String testVar;

    @Autowired
    ParametersParser paramParser;

    @Autowired
    HelpProvider helpProvider;

    @Autowired
    BitcoindCaller daemonImpl;

    @Autowired
    AppExecutor appExecutor;

    private Map<CommandLineOperation, Consumer> actionMap;

    public static void main(String[] args) throws Exception {

        SpringApplication.run(BTCKillerDatabaseApp.class, args);

    }

    @PostConstruct
    public void init(){
        actionMap = new EnumMap<>(CommandLineOperation.class);
        actionMap.put(CommandLineOperation.HELP, o -> helpProvider.provideHelp());
        actionMap.put(CommandLineOperation.TEST, o -> {
            daemonImpl.getBlockchainSize();
            daemonImpl.getBlock(100000);
            appExecutor.loadBlockChain();

        });

    }

    @Override
    public void run(String[] args) throws Exception {
        System.out.println("Started bitcoin-killer-database");
        CommandLineOperation operation = paramParser.getOperation(args);
        actionMap.get(operation).accept(operation);
    }
}
