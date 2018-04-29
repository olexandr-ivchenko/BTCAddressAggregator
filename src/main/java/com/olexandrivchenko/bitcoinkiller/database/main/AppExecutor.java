package com.olexandrivchenko.bitcoinkiller.database.main;

public interface AppExecutor {

    void startBlockChainIndexMaintain();

    void loadBlockChain();

}
