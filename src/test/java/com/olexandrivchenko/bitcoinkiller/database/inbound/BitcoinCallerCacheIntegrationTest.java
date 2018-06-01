package com.olexandrivchenko.bitcoinkiller.database.inbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olexandrivchenko.bitcoinkiller.database.inbound.cache.BitcoindCallerCacheImpl;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.main.AddressSet;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = {BitcoindCallerCacheImpl.class, BitcoindCallerImpl.class, BitcoindServiceImpl.class,
//        LoggingCacheListener.class})
@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
public class BitcoinCallerCacheIntegrationTest {

    @Autowired
    @Qualifier("BitcoindCallerCache")
    private BitcoindCallerCacheImpl daemon;

    @Test
    @Ignore
    public void testDuplicateVoutRead() {
        long startBlock = 450471;
        long endBlock = 451001;
        AddressSet addressSet = new AddressSet();
        for (Long i = startBlock; i <= endBlock; i++) {
            //load block
            GenericResponse<Block> block = daemon.getBlock(i);
            daemon.cleanCacheFromBlockInfo(block.getResult());

        }
        assertEquals("Cache should not evict during test", 0, daemon.getCacheStatistics().cacheEvictedCount());

    }

    @Test
    @Ignore
    public void loadBlockAndLog() throws JsonProcessingException {
        GenericResponse<Block> block = daemon.getBlock(481800);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(block));
    }

}
