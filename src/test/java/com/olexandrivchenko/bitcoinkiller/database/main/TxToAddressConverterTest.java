package com.olexandrivchenko.bitcoinkiller.database.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCallerImpl;
import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindServiceImpl;
import com.olexandrivchenko.bitcoinkiller.database.inbound.cache.BitcoindCallerCacheImpl;
import com.olexandrivchenko.bitcoinkiller.database.inbound.cache.LoggingCacheListener;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Tx;
import com.olexandrivchenko.bitcoinkiller.database.outbound.dto.Address;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BitcoindServiceImpl.class, BitcoindCallerCacheImpl.class, BitcoindCallerImpl.class,
        TxToAddressConverter.class, LoggingCacheListener.class})
public class TxToAddressConverterTest {

    @Autowired
    TxToAddressConverter converter;

    /**
     * Please refer to https://github.com/olexandr-ivchenko/btcKillerDatabase/issues/3
     * block height 118398
     * transactionId 9056cc7c2181dbd1d2ec6bd92b3d7628f1811b898b009895edfd587e6c5c0cb3
     * Dumped to file 118398_9056cc7c2181dbd1d2ec6bd92b3d7628f1811b898b009895edfd587e6c5c0cb3.json
     */
    @Test
    public void testIssue3Transaction() throws IOException {
        String jsonBody = IOUtils.toString(
                this.getClass().getResourceAsStream("/com/olexandrivchenko/bitcoinkiller/database/transactions/118398_9056cc7c2181dbd1d2ec6bd92b3d7628f1811b898b009895edfd587e6c5c0cb3.json"),
                "UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        Tx tx = mapper.readValue(jsonBody, Tx.class);
        List<Address> result = converter.convert(tx);

        assertEquals(14, result.size());
        double sum = 0;
        for (Address addr : result) {
            sum += addr.getAmount();
        }
        assertEquals(0, sum, 0.000000001);
        assertEquals("184wu9LYeh8q73i9CHWNA8GR5CDZUMQeSw", result.get(13).getAddress());
    }

}
