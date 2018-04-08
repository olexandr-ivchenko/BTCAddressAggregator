package com.olexandrivchenko.bitcoinkiller.database.inbound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlockchainParser {

    @Autowired
    private BitcoindCaller bitcoind;


    public void getFullBlockInfo(long blockNumber){
//        bitcoind.
    }
}
