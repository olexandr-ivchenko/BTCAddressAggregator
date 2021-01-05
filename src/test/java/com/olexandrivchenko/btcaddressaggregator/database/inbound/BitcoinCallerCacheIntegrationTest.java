package com.olexandrivchenko.btcaddressaggregator.database.inbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.cache.BitcoindCallerCacheImpl;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.btcaddressaggregator.database.main.AddressSet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
//@ContextConfiguration(classes = {BitcoindCallerCacheImpl.class, BitcoindCallerImpl.class, BitcoindServiceImpl.class,
//        LoggingCacheListener.class})
@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
public class BitcoinCallerCacheIntegrationTest {

    @Autowired
    @Qualifier("BitcoindCallerCache")
    private BitcoindCallerCacheImpl daemon;

    @Test
    @Disabled
    public void testDuplicateVoutRead() {
        long startBlock = 450471;
        long endBlock = 451001;
        AddressSet addressSet = new AddressSet();
        for (Long i = startBlock; i <= endBlock; i++) {
            //load block
            GenericResponse<Block> block = daemon.getBlock(i);
            daemon.cleanCacheFromBlockInfo(block.getResult());

        }
        assertEquals(0, daemon.getCacheStatistics().cacheEvictedCount(), "Cache should not evict during test");

    }

    @Test
    @Disabled
    public void loadBlockAndLog() throws JsonProcessingException {
        GenericResponse<Block> block = daemon.getBlock(481800);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(block));
    }

}
