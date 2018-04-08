package com.olexandrivchenko.bitcoinkiller.database.main;

import com.olexandrivchenko.bitcoinkiller.database.outbound.Address;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class AddressSetTest {

    @Test
    public void testSingleAdd(){
        AddressSet set = new AddressSet();
        Address addr = generateAddress(null, null, 0);
        set.add(addr);
        assertEquals("Added single record - expecting single entry", 1, set.getAddresses().size());
    }

    @Test
    public void testAddFewItems(){
        AddressSet set = new AddressSet();
        set.add(generateAddress(null, null, 0));
        set.add(generateAddress(null, null, 0));
        set.add(generateAddress(null, null, 0));
        assertEquals("Added 3 records - expecting 3", 3, set.getAddresses().size());
    }

    @Test
    public void testAddSameItem(){
        AddressSet set = new AddressSet();
        set.add(generateAddress("xxxyyyzzz", 50d, 10));
        set.add(generateAddress("xxxyyyzzz", -10d, 50));
        set.add(generateAddress("xxxyyyzzz", -20d, 80));
        assertEquals("same address 3 times - should give one result", 1, set.getAddresses().size());
        assertEquals("One block minus 2 transactions", 20d, set.getAddresses().get("xxxyyyzzz").getAmount(), 0.000000001);
        assertEquals("last changed on block 80", 80, set.getAddresses().get("xxxyyyzzz").getLastSeenBlock());
    }


    private Address generateAddress(String address, Double amount, long lastBlock){
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
        return addr;
    }

}
