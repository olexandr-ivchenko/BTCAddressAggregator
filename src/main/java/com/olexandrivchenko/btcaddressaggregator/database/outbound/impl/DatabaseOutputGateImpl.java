package com.olexandrivchenko.btcaddressaggregator.database.outbound.impl;

import com.olexandrivchenko.btcaddressaggregator.database.outbound.OutputGate;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.Address;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.DbUpdateLog;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.repository.AddressRepository;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.repository.DbStateRepository;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.repository.DbUpdateLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is a class that should be used to work with database
 * It performs all logic for updating already existing addresses
 */
@Service
public class DatabaseOutputGateImpl implements OutputGate {

    private final static Logger log = LoggerFactory.getLogger(DatabaseOutputGateImpl.class);

    private final AddressRepository addressRepo;
    private final DbStateRepository dbState;
    private final DbUpdateLogRepository dbUpdateLogRepository;

    public DatabaseOutputGateImpl(AddressRepository addressRepo,
                                  DbStateRepository dbState,
                                  DbUpdateLogRepository dbUpdateLogRepository) {
        this.addressRepo = addressRepo;
        this.dbState = dbState;
        this.dbUpdateLogRepository = dbUpdateLogRepository;
    }

    @Override
    @NonNull
    public synchronized DbUpdateLog getJobToProcess(int size, boolean skipUnfinished) {
        DbUpdateLog lastLog = dbUpdateLogRepository.findFirstByOrderByEndBlockDesc();
        if (skipUnfinished) {
            DbUpdateLog unfinished = dbUpdateLogRepository.findFirstByProcessedFalseOrderByStartBlockAsc();
            if (unfinished != null) {
                if (unfinished.getId().equals(lastLog.getId())) {
                    dbUpdateLogRepository.delete(unfinished);
                    lastLog = dbUpdateLogRepository.findFirstByOrderByEndBlockDesc();
                } else {
                    return unfinished;
                }
            }
        }
        DbUpdateLog newJob = new DbUpdateLog();
        if (lastLog == null) {
            newJob.setStartBlock(0L);
        } else {
            newJob.setStartBlock(lastLog.getEndBlock() + 1);
        }
        newJob.setEndBlock(newJob.getStartBlock() + size - 1);
        newJob.setProcessed(false);
        dbUpdateLogRepository.save(newJob);
        return newJob;
    }

    @Override
    public long getNewJobStartPoint(boolean skipUnfinished) {
        if (skipUnfinished) {
            DbUpdateLog unfinished = dbUpdateLogRepository.findFirstByProcessedFalseOrderByStartBlockAsc();
            if (unfinished != null) {
                return unfinished.getStartBlock();
            }
        }
        DbUpdateLog lastLog = dbUpdateLogRepository.findFirstByOrderByEndBlockDesc();
        if (lastLog == null) {
            return 0L;
        } else {
            return lastLog.getEndBlock() + 1;
        }
    }

    @Override
    @Transactional
    public void runUpdate(@NonNull Map<String, Address> addressesMap, DbUpdateLog job) {
        List<Address> existing = loadExistingAddresses(addressesMap);
        mergeExistingIntoUpdate(addressesMap, existing);
        job.setProcessed(true);
        if (!addressesMap.isEmpty()) {
            addressRepo.saveAll(addressesMap.values());
//            addressRepo.flush();
        }
        dbUpdateLogRepository.save(job);
        int deleted = addressRepo.wipeZeroBalance();
        log.info("Deleted {} zero addresses", deleted);
    }

    private void mergeExistingIntoUpdate(Map<String, Address> addresses, List<Address> existing) {
        for (Address addr : existing) {
            Address address = addresses.get(addr.getAddress());
            address.setId(addr.getId());
            address.setLastSeenBlock(Math.max(address.getLastSeenBlock(), addr.getLastSeenBlock()));
            address.setAmount(address.getAmount() + addr.getAmount());
        }
    }

    private List<Address> loadExistingAddresses(@NonNull Map<String, Address> addresses) {
        List<Address> existingAddresses = new ArrayList<>();
        List<String> addr = new ArrayList<>(addresses.keySet());
        int chunkSize = 200;
        for (int i = 0; i < addr.size() / chunkSize + 1; i++) {
            List<String> sub = addr.subList(i * chunkSize, Math.min((i + 1) * chunkSize, addr.size()));
            if (sub.size() > 0) {
                existingAddresses.addAll(addressRepo.getExistingAddresses(sub));
            }
        }
        return existingAddresses;
    }

}
