package com.olexandrivchenko.btcaddressaggregator.database;

import com.olexandrivchenko.btcaddressaggregator.database.inbound.BitcoindCaller;
import com.olexandrivchenko.btcaddressaggregator.database.main.AppExecutor;
import com.olexandrivchenko.btcaddressaggregator.database.main.HelpProvider;
import com.olexandrivchenko.btcaddressaggregator.database.main.ParametersParser;
import com.olexandrivchenko.btcaddressaggregator.database.main.dto.CommandLineOperation;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.fileexport.AddressExportGate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

@SpringBootApplication
@EnableScheduling
public class BTCAddressAggregator implements CommandLineRunner {
    private final static Logger log = LoggerFactory.getLogger(BTCAddressAggregator.class);

    private final ParametersParser paramParser;
    private final HelpProvider helpProvider;
    private final BitcoindCaller daemonImpl;
    private final AppExecutor appExecutor;
    private final AddressExportGate addressExportGate;

    private Map<CommandLineOperation, Consumer> actionMap;

    public BTCAddressAggregator(ParametersParser paramParser,
                                HelpProvider helpProvider,
                                @Qualifier("BitcoindCallerCache") BitcoindCaller daemonImpl,
                                AppExecutor appExecutor,
                                AddressExportGate addressExportGate) {
        this.paramParser = paramParser;
        this.helpProvider = helpProvider;
        this.daemonImpl = daemonImpl;
        this.appExecutor = appExecutor;
        this.addressExportGate = addressExportGate;
    }

    public static void main(String[] args) {
        SpringApplication.run(BTCAddressAggregator.class, args);
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
        actionMap.put(CommandLineOperation.EXPORT, o->{
            try {
                addressExportGate.exportDatabaseToFile();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //really???
            System.exit(0);
        });

    }

    @Override
    public void run(String[] args) {
        System.out.println("Started BTCAddressAggregator");
        CommandLineOperation operation = paramParser.getOperation(args);
        actionMap.get(operation).accept(operation);
    }
}
