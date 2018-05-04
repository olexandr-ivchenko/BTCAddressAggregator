package com.olexandrivchenko.bitcoinkiller.database.inbound.cache;

import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Tx;

public class CachedTx {
    private Tx tx;
    private int readCount = 0;

    public CachedTx(Tx tx) {
        this.tx = tx;
    }

    public int getOutCount(){
        return tx.getVout().size();
    }

    public int getReadCount() {
        return readCount;
    }

    public Tx getTx() {
        return tx;
    }

    public void notifyRead(){
        readCount++;
    }
}
