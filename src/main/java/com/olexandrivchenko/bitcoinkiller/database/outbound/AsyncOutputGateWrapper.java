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
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

@Service
public class AsyncOutputGateWrapper {
    private final static Logger log = LoggerFactory.getLogger(AsyncOutputGateWrapper.class);

    private Queue<Map.Entry<DbUpdateLog, Map<String, Address>>> updateQueue = new ArrayBlockingQueue<>(5);
    private OutputGate outputGate;

    public AsyncOutputGateWrapper(OutputGate outputGate) {
        this.outputGate = outputGate;
    }

    @NonNull
    public synchronized DbUpdateLog getJobToProcess(int size) {
        if(updateQueue.isEmpty()){
            return outputGate.getJobToProcess(size, true);
        }
        return outputGate.getJobToProcess(size, false);
    }

    public void postUpdateJob(Map<String, Address> addresses, DbUpdateLog job) {
        updateQueue.add(new AbstractMap.SimpleEntry<>(job, addresses));
    }

    @Scheduled(fixedDelay = 1000)
    public void runAsyncUpdate() {
        Map.Entry<DbUpdateLog, Map<String, Address>> job;
        while ((job = updateQueue.poll()) != null) {
            long start = System.currentTimeMillis();
            outputGate.runUpdate(job.getValue(), job.getKey());
            log.info("Updated database with job {}-{} address count={} in {} ms",
                    job.getKey().getStartBlock(),
                    job.getKey().getEndBlock(),
                    job.getValue().size(),
                    System.currentTimeMillis() - start);
        }
    }

}
