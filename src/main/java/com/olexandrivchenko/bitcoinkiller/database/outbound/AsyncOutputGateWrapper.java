package com.olexandrivchenko.bitcoinkiller.database.outbound;

import com.olexandrivchenko.bitcoinkiller.database.outbound.dto.Address;
import com.olexandrivchenko.bitcoinkiller.database.outbound.dto.DbUpdateLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class AsyncOutputGateWrapper {
    private final static Logger log = LoggerFactory.getLogger(AsyncOutputGateWrapper.class);

    private BlockingQueue<Map.Entry<DbUpdateLog, Map<String, Address>>> updateQueue = new ArrayBlockingQueue<>(2);
    private OutputGate outputGate;

    public AsyncOutputGateWrapper(OutputGate outputGate) {
        this.outputGate = outputGate;
    }

    @NonNull
    public DbUpdateLog getJobToProcess(int size) {
        if (isUpdateQueueEmpty()) {
            return outputGate.getJobToProcess(size, true);
        }
        return outputGate.getJobToProcess(size, false);
    }

    public long getNewJobStartPoint() {
        if (isUpdateQueueEmpty()) {
            return outputGate.getNewJobStartPoint(true);
        }
        return outputGate.getNewJobStartPoint(false);
    }

    public void postUpdateJob(Map<String, Address> addresses, DbUpdateLog job) {
        boolean result = false;
        do {
            try {
                long start = System.currentTimeMillis();
                result = updateQueue.offer(new AbstractMap.SimpleEntry<>(job, addresses), 10, TimeUnit.SECONDS);
                long end = System.currentTimeMillis();
                if (end - start > 1000) {
                    log.info("Adding job {}-{} with result [{}] to queue had to wait for {}ms",
                            job.getStartBlock(),
                            job.getEndBlock(),
                            result,
                            end - start);
                }
            } catch (InterruptedException e) {
                log.error("Strange exception, while waiting to add job to queue");
            }
        } while (!result);
    }

    private boolean isUpdateQueueEmpty() {
        return updateQueue.isEmpty() && !isRunningUpdate;
    }

    private boolean isRunningUpdate = false;

    @Scheduled(fixedDelay = 1000)
    public synchronized void runAsyncUpdate() {
        isRunningUpdate = true;
        try {
            Map.Entry<DbUpdateLog, Map<String, Address>> job;
            while ((job = updateQueue.poll()) != null) {
                long start = System.currentTimeMillis();
                log.info("Database job {}-{} submited with {} addresses",
                        job.getKey().getStartBlock(),
                        job.getKey().getEndBlock(),
                        job.getValue().size());
                outputGate.runUpdate(job.getValue(), job.getKey());
                log.info("Updated database with job {}-{} address count={} in {} ms",
                        job.getKey().getStartBlock(),
                        job.getKey().getEndBlock(),
                        job.getValue().size(),
                        System.currentTimeMillis() - start);
            }
        } catch (Throwable e) {
            log.error("Fatal exception in database update thread", e);
            System.exit(1);
        } finally {
            isRunningUpdate = false;
        }
    }

}
