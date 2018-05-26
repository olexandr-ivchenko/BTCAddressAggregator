package com.olexandrivchenko.bitcoinkiller.database.main;

import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCaller;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Tx;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Vin;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Vout;
import com.olexandrivchenko.bitcoinkiller.database.outbound.dto.Address;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class TxToAddressConverter {

    private final BitcoindCaller daemon;

    public TxToAddressConverter(@Qualifier("BitcoindCallerCache") BitcoindCaller daemon) {
        this.daemon = daemon;
    }

    public List<Address> convert(Tx tx) {
        List<Vin> vin = tx.getVin();
        List<Vout> vout = tx.getVout();

        List<Address> rs = new ArrayList<>();
        if (!isBlockReward(vin)) {
            for (Vin vinn : vin) {
//                Tx inputTransaction = daemon.loadTransaction(vinn.getTxid());
//                Vout outForThisInput = inputTransaction.getVout().get(vinn.getVout());
                Vout outForThisInput = daemon.getTransactionOut(vinn.getTxid(), vinn.getVout());
                Address inputAddr = getAddress(outForThisInput, false);
                rs.add(inputAddr);
            }
        }
        //dirty hack to see the outcome
        tx.setVin(null);
        for (Vout voutt : vout) {
            Address outAddr = getAddress(voutt, true);
            rs.add(outAddr);
        }
        return rs;
    }

    private boolean isBlockReward(List<Vin> vin) {
        return vin.size() == 1
                && vin.get(0).getCoinbase() != null
                && vin.get(0).getTxid() == null
                && vin.get(0).getScriptSig() == null;
    }

    private Address getAddress(Vout out, boolean isAdded) {
        Address addr = new Address();
        addr.setAmount(isAdded ? out.getValue() : -out.getValue());
        List<String> addresses = out.getScriptPubKey().getAddresses();
        if (addresses != null) {
            addr.setAddress(addresses.get(0));
            if (addresses.size() > 1) {
                Random rand = new Random();
                addr.setAddress("err" + rand.nextInt());
//                throw new Error("review this!!!");
            }
        } else {
            Random rand = new Random();
            addr.setAddress("err" + rand.nextInt());
        }
        return addr;
    }

}
