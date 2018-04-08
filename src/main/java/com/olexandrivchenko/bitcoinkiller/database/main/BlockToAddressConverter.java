package com.olexandrivchenko.bitcoinkiller.database.main;

import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Tx;
import com.olexandrivchenko.bitcoinkiller.database.outbound.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BlockToAddressConverter {

    @Autowired
    private TxToAddressConverter txConverter;

    public List<Address> convert(Block block){
        List<Address> result = new ArrayList<>();
        for(Tx tx : block.getTx()){
            List<Address> txResults = txConverter.convert(tx);
            result.addAll(txResults);
        }
        return result;
    }
}
