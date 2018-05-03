package com.olexandrivchenko.bitcoinkiller.database.main;

import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCaller;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.outbound.OutputGate;
import com.olexandrivchenko.bitcoinkiller.database.outbound.dto.Address;
import com.olexandrivchenko.bitcoinkiller.database.outbound.dto.DbUpdateLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class AppExecutorImpl implements AppExecutor, Runnable{
    private final static Logger log = LoggerFactory.getLogger(AppExecutorImpl.class);

    private final static int BLOCKS_TO_PROCESS_IN_ONE_BATCH = 1000;

    private BitcoindCaller daemon;
    private BlockToAddressConverter blockConverter;
    private OutputGate out;

    public AppExecutorImpl(@Qualifier("BitcoindCallerCache") BitcoindCaller daemon,
                           BlockToAddressConverter blockConverter,
                           OutputGate out) {
        this.daemon = daemon;
        this.blockConverter = blockConverter;
        this.out = out;
    }

    @Override
    public void startBlockChainIndexMaintain(){
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this, 0, 1, TimeUnit.MINUTES);

    }

    @Override
    public void loadBlockChain(){
        while(true) {
            long startTime = System.currentTimeMillis();
            //check if there is job to process
            long blockchainSize = daemon.getBlockchainSize();
            long processedBlocks = out.getLastProcessedBlockNumber();
            if (processedBlocks >= blockchainSize) {
                log.info("BlockChain is up to date - going to sleep");
                return;
            }
            //get next job to process
            DbUpdateLog job = out.getJobToProcess(Math.min(BLOCKS_TO_PROCESS_IN_ONE_BATCH, (int)(blockchainSize-processedBlocks)));
            log.info("Going to process blocks {}-{}", job.getStartBlock(), job.getEndBlock());
            AddressSet addressSet = new AddressSet();
            for (Long i = job.getStartBlock(); i <= job.getEndBlock(); i++) {
                //load block
                GenericResponse<Block> block = daemon.getBlock(i);

                //process block
                List<Address> blockAddressChanges = blockConverter.convert(block.getResult());
                addressSet.addAll(blockAddressChanges);
            }
            double blocksSum = addressSet.getAddresses().values().stream().map(Address::getAmount).mapToDouble(Double::doubleValue).sum();

            job.setProcessed(true);
            log.info("Going to save {} addresses", addressSet.getAddresses().size());
            //save results to DB
            out.runUpdate(addressSet.getAddresses(), job);
            log.info("Done blocks {}-{} in {} seconds", job.getStartBlock(), job.getEndBlock(), (System.currentTimeMillis()-startTime)/1000);
        }
    }

    @Override
    public void run() {
        try {
            loadBlockChain();
        }catch (Throwable e){
            //TODO review this. Catching everything is generally bad approach
            log.error("Got exception, during blockchain sync", e);
            System.exit(1);
        }
    }
}
