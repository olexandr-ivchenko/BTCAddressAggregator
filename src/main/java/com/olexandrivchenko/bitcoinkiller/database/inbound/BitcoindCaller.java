package com.olexandrivchenko.bitcoinkiller.database.inbound;

import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Tx;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Vout;

public interface BitcoindCaller {

    long getBlockchainSize();

    GenericResponse<Block> getBlock(long number);

    Tx loadTransaction(String txid);

    default Vout getTransactionOut(String txid, Integer n){
        throw new UnsupportedOperationException("getTransactionOut is not supported");
    }

}
