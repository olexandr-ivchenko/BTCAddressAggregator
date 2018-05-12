package com.olexandrivchenko.bitcoinkiller.database.main;

import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCaller;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.outbound.AsyncOutputGateWrapper;
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
    private AsyncOutputGateWrapper asyncOut;

    public AppExecutorImpl(@Qualifier("BitcoindCallerCache") BitcoindCaller daemon,
                           BlockToAddressConverter blockConverter,
                           OutputGate out,
                           AsyncOutputGateWrapper asyncOut) {
        this.daemon = daemon;
        this.blockConverter = blockConverter;
        this.out = out;
        this.asyncOut = asyncOut;
    }

    @Override
    public void startBlockChainIndexMaintain(){
        //temporary warm up booster
        log.info("Goin to warm up cache");
        long processedBlocks = out.getLastProcessedBlockNumber();
        for(int i=3000;i>1;i--){
            daemon.getBlock(processedBlocks-i);
        }

//        Random rand = new Random();
//        List<Address> duplicates = new ArrayList<>();
//        long start = 0;
//        while(true){
//            start = System.currentTimeMillis();
//            AddressSet set = new AddressSet();
//            duplicates = new ArrayList<>();
//            for(int i=0; i<100; i++){
//                List<Address> l = new ArrayList<>();
//                for(int j=0; j< 2000; j++) {
//                    Address a = new Address();
//                    a.setAmount(rand.nextInt()%2==0?0:Math.abs(rand.nextDouble()%100));
////                    a.setAmount(rand.nextInt());
//                    a.setAddress(UUID.randomUUID().toString().substring(0, 31));
//                    a.setLastSeenBlock(1000000L);
//                    a.setCreationBlock(1000000L);
//                    l.add(a);
//                    if(rand.nextInt()%2 == 0){
//                        Address dup = new Address();
//                        dup.setCreationBlock(1000000L);
//                        dup.setLastSeenBlock(1000000L);
//                        dup.setAddress(a.getAddress());
//                        dup.setAmount(-a.getAmount() + Math.abs(rand.nextInt()%2));
//                        duplicates.add(dup);
//                    }
//                }
//                set.addAll(l);
//            }
//            DbUpdateLog dbUpdateLog = new DbUpdateLog();
//            dbUpdateLog.setProcessed(false);
//            dbUpdateLog.setStartBlock(1000000L);
//            dbUpdateLog.setEndBlock(1000001L);
//            asyncOut.postUpdateJob(set.getAddresses(), dbUpdateLog);
//        }

        log.info("startBlockChainIndexMaintain");
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
            DbUpdateLog job = asyncOut.getJobToProcess(Math.min(BLOCKS_TO_PROCESS_IN_ONE_BATCH, (int)(blockchainSize-processedBlocks)));

            log.info("Going to process blocks {}-{}", job.getStartBlock(), job.getEndBlock());
            AddressSet addressSet = new AddressSet();
            for (Long i = job.getStartBlock(); i <= job.getEndBlock(); i++) {
                //load block
                GenericResponse<Block> block = daemon.getBlock(i);

                //process block
                List<Address> blockAddressChanges = blockConverter.convert(block.getResult());
                addressSet.addAll(blockAddressChanges);
            }

            job.setProcessed(true);
            //save results to DB
            asyncOut.postUpdateJob(addressSet.getAddresses(), job);
            log.info("Done blocks {}-{} with address count={} in {} seconds",
                    job.getStartBlock(),
                    job.getEndBlock(),
                    addressSet.getAddresses().size(),
                    (System.currentTimeMillis()-startTime)/1000);
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
