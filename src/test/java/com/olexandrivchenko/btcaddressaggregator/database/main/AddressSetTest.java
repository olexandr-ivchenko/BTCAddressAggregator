package com.olexandrivchenko.btcaddressaggregator.database.main;

import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.Address;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddressSetTest {

    @Test
    public void testSingleAdd(){
        AddressSet set = new AddressSet();
        set.addAll(generateAddressList(null, null, 0));
        assertEquals(1, set.getAddresses().size(), "Added single record - expecting single entry");
    }

    @Test
    public void testAddFewItems(){
        AddressSet set = new AddressSet();
        set.addAll(generateAddressList(null, null, 0));
        set.addAll(generateAddressList(null, null, 0));
        set.addAll(generateAddressList(null, null, 0));
        assertEquals(3, set.getAddresses().size(), "Added 3 records - expecting 3");
    }

    @Test
    public void testAddSameItem(){
        AddressSet set = new AddressSet();
        set.addAll(generateAddressList("xxxyyyzzz", 50d, 10));
        set.addAll(generateAddressList("xxxyyyzzz", -10d, 50));
        set.addAll(generateAddressList("xxxyyyzzz", -20d, 80));
        assertEquals(1, set.getAddresses().size(), "same address 3 times - should give one result");
        assertEquals(20d, set.getAddresses().get("xxxyyyzzz").getAmount(), 0.000000001, "One block minus 2 transactions");
        assertEquals(80, set.getAddresses().get("xxxyyyzzz").getLastSeenBlock(), "last changed on block 80");
    }

    public void testRemoveZeroBalances(){
        AddressSet set = new AddressSet();
        set.addAll(generateAddressList(null, 0d, 10));
        set.addAll(generateAddressList(null, 0d, 15));
        set.addAll(generateAddressList(null, 0d, 18));
        assertEquals(0, set.getAddresses().size(), "Empty addresses should be removed");

    }


    private List<Address> generateAddressList(String address, Double amount, long lastBlock){
        if(address == null){
            address = UUID.randomUUID().toString();
        }
        if(amount == null){
            amount = 50d;
        }
        Address addr = new Address();
        addr.setAmount(amount);
        addr.setAddress(address);
        addr.setLastSeenBlock(lastBlock);
        List<Address> rs = new ArrayList<>();
        rs.add(addr);
        return rs;
    }

}
