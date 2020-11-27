package com.olexandrivchenko.btcaddressaggregator.database.main;

public interface AppExecutor {

    void startBlockChainIndexMaintain();

    void loadBlockChain();

}
