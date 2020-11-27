package com.olexandrivchenko.btcaddressaggregator.database.main;

import com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc.Tx;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.Address;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BlockToAddressConverter {

    private final TxToAddressConverter txConverter;

    public BlockToAddressConverter(TxToAddressConverter txConverter) {
        this.txConverter = txConverter;
    }

    public List<Address> convert(Block block){
        List<Address> result = new ArrayList<>();
        for(Tx tx : block.getTx()){
            List<Address> txResults = txConverter.convert(tx);
            txResults.forEach(o->{
                o.setLastSeenBlock(block.getHeight());
                o.setCreationBlock(block.getHeight());
            });
            result.addAll(txResults);
        }
        return result;
    }

}
