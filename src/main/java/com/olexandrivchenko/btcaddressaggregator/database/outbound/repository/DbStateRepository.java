package com.olexandrivchenko.btcaddressaggregator.database.outbound.repository;

import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.DbState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DbStateRepository extends JpaRepository<DbState, Long>{
}
