package com.olexandrivchenko.bitcoinkiller.database.outbound.repository;

import com.olexandrivchenko.bitcoinkiller.database.outbound.dto.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("select a from Address a where a.address in :addresses")
    List<Address> getExistingAddresses(@Param("addresses") Collection<String> addresses);

    @Modifying
    @Query("delete from Address a where a.amount>=-0.00000001 and a.amount<=0.00000001")
    int wipeZeroBalance();
}
