package com.olexandrivchenko.btcaddressaggregator.database.main;

import com.olexandrivchenko.btcaddressaggregator.database.inbound.BitcoindCaller;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.cache.BitcoindCallerCacheImpl;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.AsyncOutputGateWrapper;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.Address;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.DbUpdateLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class AppExecutorImpl implements AppExecutor, Runnable {
    private final static Logger log = LoggerFactory.getLogger(AppExecutorImpl.class);

    private final static int BLOCKS_TO_PROCESS_IN_ONE_BATCH = 10;

    private BitcoindCaller daemon;
    private BlockToAddressConverter blockConverter;
    private AsyncOutputGateWrapper asyncOut;

    public AppExecutorImpl(@Qualifier("BitcoindCallerCache") BitcoindCaller daemon,
                           BlockToAddressConverter blockConverter,
                           AsyncOutputGateWrapper asyncOut) {
        this.daemon = daemon;
        this.blockConverter = blockConverter;
        this.asyncOut = asyncOut;
    }

    @Override
    public void startBlockChainIndexMaintain() {
        warmUpCache();

        log.info("startBlockChainIndexMaintain");
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this, 0, 1, TimeUnit.MINUTES);

    }

    private void warmUpCache() {
        log.info("Goin to warm up cache");
        long blockchainSize = daemon.getBlockchainSize();
        long lastBlockToWarmUp = asyncOut.getNewJobStartPoint()-1;
        int blocksToWarmUp = Math.min(5000, (int)(blockchainSize-lastBlockToWarmUp)/2);
        blocksToWarmUp = Math.max(blocksToWarmUp, 200);
        log.info("Blocks to warm up {}-{}, total {}",
                lastBlockToWarmUp-blocksToWarmUp,
                lastBlockToWarmUp,
                blocksToWarmUp);
        for (int i = blocksToWarmUp; i > 1; i--) {
            GenericResponse<Block> block = daemon.getBlock(lastBlockToWarmUp - i);
            if (daemon instanceof BitcoindCallerCacheImpl) {
                ((BitcoindCallerCacheImpl) daemon).cleanCacheFromBlockInfo(block.getResult());
            }
        }
    }

    @Override
    public void loadBlockChain() {
        while (true) {
            long startTime = System.currentTimeMillis();
            //check if there is job to process
            long blockchainSize = daemon.getBlockchainSize();
            long processedBlocks = asyncOut.getNewJobStartPoint();
            if (processedBlocks >= blockchainSize) {
                log.info("BlockChain is up to date - going to sleep");
                return;
            }
            //get next job to process
            DbUpdateLog job = asyncOut.getJobToProcess(Math.min(BLOCKS_TO_PROCESS_IN_ONE_BATCH, (int) (blockchainSize - processedBlocks)));

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
                    (System.currentTimeMillis() - startTime) / 1000);
        }
    }

    @Override
    public void run() {
        try {
            loadBlockChain();
        } catch (Throwable e) {
            //TODO review this. Catching everything is generally bad approach
            log.error("Got exception, during blockchain sync", e);
            System.exit(1);
        }
    }
}
