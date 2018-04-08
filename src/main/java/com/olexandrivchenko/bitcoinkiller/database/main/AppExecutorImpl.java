package com.olexandrivchenko.bitcoinkiller.database.main;

import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCaller;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.outbound.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppExecutorImpl implements AppExecutor{
    private final static Logger log = LoggerFactory.getLogger(AppExecutorImpl.class);

    @Autowired
    private BitcoindCaller daemon;

    @Autowired
    private BlockToAddressConverter blockConverter;

    public void loadBlockChain(){
        //get next block to process
        long nextBlock = 100000;
        List<Address> addr = new ArrayList<>();
        AddressSet addressSet = new AddressSet();
        for(int i=0; i < 1000; i++) {
            //load block
            GenericResponse<Block> block = daemon.getBlock(nextBlock);

            //process block
            List<Address> blockAddressChanges = blockConverter.convert(block.getResult());
            addr.addAll(blockAddressChanges);
            addressSet.addAll(blockAddressChanges);
            nextBlock++;
        }
        double blocksSum = addressSet.getAddresses().values().stream().map(Address::getAmount).mapToDouble(Double::doubleValue).sum();
        if(blocksSum != 50000){
            throw new Error("check this");
        }
        log.info("Found {} addresses", addr.size());
        //save results to DB
    }
}
