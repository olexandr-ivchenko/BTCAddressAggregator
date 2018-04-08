package com.olexandrivchenko.bitcoinkiller.database.inbound;

import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Tx;

public interface BitcoindCaller {

    long getBlockchainSize();

    GenericResponse<Block> getBlock(long number);

    Tx loadTransaction(String txid);
}
