package com.olexandrivchenko.btcaddressaggregator.database.outbound.impl;

import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressCsvRenderer {

    public String renderAddress(Address address){
        return address.getAddress() + "," +
                address.getCreationBlock() + "," +
                address.getLastSeenBlock() + "," +
                address.getAmount() + "\n";
    }

    public Address decodeAddress(String csv){
        String[] parts = csv.split(",");
        if(parts.length != 4){
            throw new RuntimeException("Address csv line is incorrect: " + csv);
        }
        Address a = new Address();
        a.setAddress(parts[0]);
        a.setCreationBlock(Long.parseLong(parts[1]));
        a.setLastSeenBlock(Long.parseLong(parts[2]));
        a.setAmount(Double.parseDouble(parts[3]));
        return a;
    }
}
