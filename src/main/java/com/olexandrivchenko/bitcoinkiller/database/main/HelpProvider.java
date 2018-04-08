package com.olexandrivchenko.bitcoinkiller.database.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HelpProvider {
    private final static Logger log = LoggerFactory.getLogger(HelpProvider.class);

    public void provideHelp() {
        log.info("------------------------------------------------------------");
        log.info("bitcoin-killer-database help");
        log.info("------------------------------------------------------------");

    }
}
