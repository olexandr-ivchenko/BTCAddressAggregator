package com.olexandrivchenko.btcaddressaggregator.database.outbound.repository;

import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;


@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("select a from Address a where a.address in :addresses")
    List<Address> getExistingAddresses(@Param("addresses") Collection<String> addresses);

    @Modifying
    @Query("delete from Address a where a.amount>=-0.00000001 and a.amount<=0.00000001")
    int wipeZeroBalance();

    @QueryHints(value = @QueryHint(name = HINT_FETCH_SIZE, value = "" + Integer.MIN_VALUE))
    @Query("select a from Address a")
    Stream<Address> streamAllAddress();
}
