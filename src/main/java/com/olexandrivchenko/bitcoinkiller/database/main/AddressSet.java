package com.olexandrivchenko.bitcoinkiller.database.main;

import com.olexandrivchenko.bitcoinkiller.database.outbound.Address;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressSet {

    private Map<String, Address> addresses = new HashMap<>();

    public void add(Address addr){
        Address storedAddr = addresses.get(addr.getAddress());
        if(storedAddr != null){
            storedAddr.setAmount(storedAddr.getAmount()+addr.getAmount());
            storedAddr.setLastSeenBlock(Math.max(addr.getLastSeenBlock(), storedAddr.getLastSeenBlock()));
        }else{
            addresses.put(addr.getAddress(), addr);
        }
    }

    public void addAll(List<Address> addresses){
        for(Address addr : addresses){
            add(addr);
        }
    }

    public Map<String, Address> getAddresses(){
        return addresses;
    }


}
