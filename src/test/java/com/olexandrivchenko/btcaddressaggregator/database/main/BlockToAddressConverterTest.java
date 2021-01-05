package com.olexandrivchenko.btcaddressaggregator.database.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.BitcoindCaller;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.BitcoindServiceImpl;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.cache.BitcoindCallerCacheImpl;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.cache.LoggingCacheListener;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.Address;
import com.olexandrivchenko.btcaddressaggregator.database.tools.BitcoinCallerFileSystemMock;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {BlockToAddressConverter.class, BitcoindServiceImpl.class, BitcoindCallerCacheImpl.class,
        BlockToAddressConverterTest.TestConfig.class, TxToAddressConverter.class, LoggingCacheListener.class} )
public class BlockToAddressConverterTest {


    @Configuration
    public static class TestConfig {
        @Bean
        @Qualifier("BitcoindCaller")
        public BitcoindCaller getBitcoinCaller(){
            return new BitcoinCallerFileSystemMock();
        }
    }


    @Autowired
    BlockToAddressConverter converter;

    /**
     * Please refer to https://github.com/olexandr-ivchenko/BTCAddressAggregator/issues/3
     * block height 118398
     * transactionId 9056cc7c2181dbd1d2ec6bd92b3d7628f1811b898b009895edfd587e6c5c0cb3
     * Dumped to file 118398_9056cc7c2181dbd1d2ec6bd92b3d7628f1811b898b009895edfd587e6c5c0cb3.json
     */
    @Test
    public void testIssue3Transaction() throws IOException {
        String jsonBody = IOUtils.toString(
                this.getClass().getResourceAsStream("/com/olexandrivchenko/btcaddressaggregator/database/blocks/118398.json"),
                "UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        Block block = mapper.readValue(jsonBody, Block.class);
        List<Address> result = converter.convert(block);

        assertEquals(110, result.size());
        double sum = 0;
        for (Address addr : result) {
            sum += addr.getAmount();
        }
        assertEquals(50, sum, 0.000000001);
        Set<Address> expectedAddr = result.stream()
                .filter(o -> o.getAddress().equals("184wu9LYeh8q73i9CHWNA8GR5CDZUMQeSw"))
                .collect(Collectors.toSet());
        assertEquals(1, expectedAddr.size());
        assertEquals("184wu9LYeh8q73i9CHWNA8GR5CDZUMQeSw", expectedAddr.stream().findFirst().map(Address::getAddress).orElse(null));
    }


}
