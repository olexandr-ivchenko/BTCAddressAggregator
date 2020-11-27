package com.olexandrivchenko.bitcoinkiller.database.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Tx;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Vout;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TestingUtils {

    private final static Logger log = LoggerFactory.getLogger(TestingUtils.class);

    public static Block loadBlock(String blockNumber) throws IOException {
        try {
            String jsonBody = IOUtils.toString(
                    TestingUtils.class.getResourceAsStream("/com/olexandrivchenko/bitcoinkiller/database/blocks/" + blockNumber + ".json"),
                    "UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonBody, Block.class);
        } catch (Exception e) {
            log.error("Failed to load block {}", blockNumber);
            throw e;
        }
    }

    public static GenericResponse<Block> getBlockRs(Long blockNumber) throws IOException {
        Block block = loadBlock(blockNumber.toString());
        GenericResponse<Block> blockRs = new GenericResponse<>();
        blockRs.setResult(block);
        return blockRs;
    }

    public static Vout getTransactionOut(String txid, Integer n) throws IOException {
        Tx tx = loadTransaction(txid);
        return tx.getVout().stream().
                filter(vout -> n.equals(vout.getN())).findFirst().orElse(null);
    }

    public static Tx loadTransaction(String txid) throws IOException {
        try {
            String jsonBody = IOUtils.toString(
                    TestingUtils.class.getResourceAsStream("/com/olexandrivchenko/bitcoinkiller/database/transactions/" + txid + ".json"),
                    "UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonBody, Tx.class);
        } catch (Exception e) {
            log.error("Failed to load transaction {}", txid);
            throw e;
        }
    }
}
