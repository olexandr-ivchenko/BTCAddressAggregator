package com.olexandrivchenko.btcaddressaggregator.database.outbound.repository;

import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.DbUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DbUpdateLogRepository extends JpaRepository<DbUpdateLog, Long>{

    DbUpdateLog findFirstByOrderByEndBlockDesc();

    DbUpdateLog findFirstByProcessedFalseOrderByStartBlockAsc();

}
