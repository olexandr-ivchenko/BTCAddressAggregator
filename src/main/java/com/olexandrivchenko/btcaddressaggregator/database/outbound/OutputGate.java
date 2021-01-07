package com.olexandrivchenko.btcaddressaggregator.database.outbound;

import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.Address;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.DbUpdateLog;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

public interface OutputGate {
    @NonNull
    DbUpdateLog getJobToProcess(int size, boolean skipUnfinished);

    long getNewJobStartPoint(boolean skipUnfinished);

    @Transactional
    void runUpdate(@NonNull Map<String, Address> addressesMap, DbUpdateLog job);
}
