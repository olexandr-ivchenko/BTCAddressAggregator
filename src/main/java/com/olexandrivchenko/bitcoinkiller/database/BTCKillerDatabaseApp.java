package com.olexandrivchenko.bitcoinkiller.database;

import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCaller;
import com.olexandrivchenko.bitcoinkiller.database.main.AppExecutor;
import com.olexandrivchenko.bitcoinkiller.database.main.HelpProvider;
import com.olexandrivchenko.bitcoinkiller.database.main.ParametersParser;
import com.olexandrivchenko.bitcoinkiller.database.main.dto.CommandLineOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

@SpringBootApplication
@EnableScheduling
public class BTCKillerDatabaseApp implements CommandLineRunner {
    private final static Logger log = LoggerFactory.getLogger(BTCKillerDatabaseApp.class);

    private ParametersParser paramParser;
    private HelpProvider helpProvider;
    private BitcoindCaller daemonImpl;
    private AppExecutor appExecutor;

    private Map<CommandLineOperation, Consumer> actionMap;

    public BTCKillerDatabaseApp(ParametersParser paramParser,
                                HelpProvider helpProvider,
                                @Qualifier("BitcoindCallerCache") BitcoindCaller daemonImpl,
                                AppExecutor appExecutor) {
        this.paramParser = paramParser;
        this.helpProvider = helpProvider;
        this.daemonImpl = daemonImpl;
        this.appExecutor = appExecutor;
    }

    public static void main(String[] args) {
        SpringApplication.run(BTCKillerDatabaseApp.class, args);
    }

    @PostConstruct
    public void init() {
        actionMap = new EnumMap<>(CommandLineOperation.class);
        actionMap.put(CommandLineOperation.HELP, o -> helpProvider.provideHelp());
        actionMap.put(CommandLineOperation.TEST, o -> {
            long blockchainSize = daemonImpl.getBlockchainSize();
            log.info("BlockChain size is: {}", blockchainSize);
            appExecutor.startBlockChainIndexMaintain();

        });

    }

    @Override
    public void run(String[] args) {
        System.out.println("Started bitcoin-killer-database");
        CommandLineOperation operation = paramParser.getOperation(args);
        actionMap.get(operation).accept(operation);
    }
}
