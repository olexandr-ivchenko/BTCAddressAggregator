package com.olexandrivchenko.bitcoinkiller.database.tools;

import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCaller;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Tx;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BitcoinCallerFileSystemMock implements BitcoindCaller {
    @Override
    public long getBlockchainSize() {
        return 0;
    }

    @Override
    public GenericResponse<Block> getBlock(long number) {
        try {
            return TestingUtils.getBlockRs(number);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Tx loadTransaction(String txid) {
        try {
            return TestingUtils.loadTransaction(txid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
