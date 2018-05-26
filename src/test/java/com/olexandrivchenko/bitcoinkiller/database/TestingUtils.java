package com.olexandrivchenko.bitcoinkiller.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class TestingUtils {

    public static Block loadBlock(String blockNumber) throws IOException {
        String jsonBody = IOUtils.toString(
                TestingUtils.class.getResourceAsStream("/com/olexandrivchenko/bitcoinkiller/database/blocks/" + blockNumber + ".json"),
                "UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonBody, Block.class);
    }

    public static GenericResponse<Block> getBlockRs(Long blockNumber) throws IOException {
        Block block = loadBlock(blockNumber.toString());
        GenericResponse<Block> blockRs = new GenericResponse<>();
        blockRs.setResult(block);
        return blockRs;
    }

}
