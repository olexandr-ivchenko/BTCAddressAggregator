package com.olexandrivchenko.bitcoinkiller.database.outbound;

import com.olexandrivchenko.bitcoinkiller.database.outbound.dto.Address;
import com.olexandrivchenko.bitcoinkiller.database.outbound.dto.DbUpdateLog;
import com.olexandrivchenko.bitcoinkiller.database.outbound.repository.AddressRepository;
import com.olexandrivchenko.bitcoinkiller.database.outbound.repository.DbStateRepository;
import com.olexandrivchenko.bitcoinkiller.database.outbound.repository.DbUpdateLogRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * This is a class that should be used to work with database
 * It performs all logic for updating already existing addresses
 */
@Service
public class OutputGate {

    private final AddressRepository addressRepo;
    private final DbStateRepository dbState;
    private final DbUpdateLogRepository dbUpdateLogRepository;

    public OutputGate(AddressRepository addressRepo, DbStateRepository dbState, DbUpdateLogRepository dbUpdateLogRepository) {
        this.addressRepo = addressRepo;
        this.dbState = dbState;
        this.dbUpdateLogRepository = dbUpdateLogRepository;
    }

    @NonNull
    public synchronized DbUpdateLog getJobToProcess(int size){
        DbUpdateLog unfinished =dbUpdateLogRepository.findFirstByProcessedFalseOrderByStartBlockAsc();
        DbUpdateLog lastLog = dbUpdateLogRepository.findFirstByOrderByEndBlockDesc();
        if(unfinished != null){
            if(unfinished.getId().equals(lastLog.getId())){
                dbUpdateLogRepository.delete(unfinished);
            }else{
                return unfinished;
            }
        }
        DbUpdateLog newJob = new DbUpdateLog();
        if(lastLog == null){
            newJob.setStartBlock(0L);
        }else{
            newJob.setStartBlock(lastLog.getEndBlock()+1);
        }
        newJob.setEndBlock(newJob.getStartBlock()+size-1);
        newJob.setProcessed(false);
        dbUpdateLogRepository.save(newJob);
        return newJob;
    }

    public long getLastProcessedBlockNumber(){
        DbUpdateLog unfinished =dbUpdateLogRepository.findFirstByProcessedFalseOrderByStartBlockAsc();
        if(unfinished != null){
            return unfinished.getStartBlock()-1;
        }
        DbUpdateLog lastLog = dbUpdateLogRepository.findFirstByOrderByEndBlockDesc();
        return ofNullable(lastLog).map(DbUpdateLog::getEndBlock).orElse(0L);
    }

    @Transactional
    public void runUpdate(Map<String, Address> addresses, DbUpdateLog job){
        List<Address> existing = loadExistingAddresses(addresses.values());
        mergeExistingIntoUpdate(addresses, existing);
        job.setProcessed(true);
        addressRepo.save(addresses.values());
        dbUpdateLogRepository.save(job);
    }

    private void mergeExistingIntoUpdate(Map<String, Address> addresses, List<Address> existing) {
        for(Address addr : existing){
            Address address = addresses.get(addr.getAddress());
            address.setId(addr.getId());
            address.setLastSeenBlock(Math.max(address.getLastSeenBlock(), addr.getLastSeenBlock()));
            address.setAmount(address.getAmount() + addr.getAmount());
        }
    }

    private List<Address> loadExistingAddresses(Collection<Address> addresses) {
        return addressRepo.getExistingAddresses(addresses.stream().map(Address::getAddress).collect(Collectors.toSet()));
    }

}
