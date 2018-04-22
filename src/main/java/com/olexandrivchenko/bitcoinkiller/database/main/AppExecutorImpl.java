package com.olexandrivchenko.bitcoinkiller.database.main;

import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCaller;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.outbound.OutputGate;
import com.olexandrivchenko.bitcoinkiller.database.outbound.dto.Address;
import com.olexandrivchenko.bitcoinkiller.database.outbound.dto.DbUpdateLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppExecutorImpl implements AppExecutor{
    private final static Logger log = LoggerFactory.getLogger(AppExecutorImpl.class);

    private final static int BLOCKS_TO_PROCESS_IN_ONE_BATCH = 100;

    private BitcoindCaller daemon;
    private BlockToAddressConverter blockConverter;
    private OutputGate out;

    public AppExecutorImpl(BitcoindCaller daemon, BlockToAddressConverter blockConverter, OutputGate out) {
        this.daemon = daemon;
        this.blockConverter = blockConverter;
        this.out = out;
    }

    public void loadBlockChain(){
        //get next job to process
        DbUpdateLog job = out.getblockNumberToProcess(BLOCKS_TO_PROCESS_IN_ONE_BATCH);

        AddressSet addressSet = new AddressSet();
        for(Long i = job.getStartBlock(); i <= job.getEndBlock(); i++) {
            //load block
            GenericResponse<Block> block = daemon.getBlock(i);

            //process block
            List<Address> blockAddressChanges = blockConverter.convert(block.getResult());
            addressSet.addAll(blockAddressChanges);
        }
        double blocksSum = addressSet.getAddresses().values().stream().map(Address::getAmount).mapToDouble(Double::doubleValue).sum();

        job.setProcessed(true);
        //save results to DB
        out.runUpdate(addressSet.getAddresses(), job);
    }

}
