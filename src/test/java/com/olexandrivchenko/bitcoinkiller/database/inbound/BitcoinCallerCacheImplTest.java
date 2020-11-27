package com.olexandrivchenko.bitcoinkiller.database.inbound;

import com.olexandrivchenko.bitcoinkiller.database.inbound.cache.BitcoindCallerCacheImpl;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Tx;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Vout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.olexandrivchenko.bitcoinkiller.database.tools.TestingUtils.getBlockRs;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class BitcoinCallerCacheImplTest {


    @Test
    public void testLoadTransaction() throws IOException {
        BitcoindCaller baseImplementation = getBitcoindCallerMock(new Long[]{118398L}, null);
        BitcoindCallerCacheImpl bitcoindCallerCache = new BitcoindCallerCacheImpl(baseImplementation, null);

        bitcoindCallerCache.getBlock(118398);
        assertEquals("Expecting block reward transaction",
                50.0d,
                bitcoindCallerCache.loadTransaction("5f0cf1cb26b2a92a5b9f22b9b20abd7ec9a6046fa8f9c6f05c0cfcf85b5c7ce7")
                        .getVout()
                        .get(0)
                        .getValue(),
                0.00000001);
        verify(baseImplementation, never()).loadTransaction(anyString());
    }

    @Test
    public void testLoadTransactionEviction() throws IOException {
        BitcoindCaller baseImplementation = getBitcoindCallerMock(new Long[]{118398L}, null);
        BitcoindCallerCacheImpl bitcoindCallerCache = new BitcoindCallerCacheImpl(baseImplementation, null);
        when(baseImplementation.loadTransaction(anyString())).thenReturn(null);

        bitcoindCallerCache.getBlock(118398);
        Tx tx = bitcoindCallerCache.loadTransaction("5f0cf1cb26b2a92a5b9f22b9b20abd7ec9a6046fa8f9c6f05c0cfcf85b5c7ce7");
        bitcoindCallerCache.loadTransaction("5f0cf1cb26b2a92a5b9f22b9b20abd7ec9a6046fa8f9c6f05c0cfcf85b5c7ce7");

        verify(baseImplementation, times(1)).getBlock(118398);
        verify(baseImplementation, times(1)).loadTransaction("5f0cf1cb26b2a92a5b9f22b9b20abd7ec9a6046fa8f9c6f05c0cfcf85b5c7ce7");
        assertEquals("Expecting block reward transaction",
                50.0d,
                tx.getVout()
                        .get(0)
                        .getValue(),
                0.00000001);

    }

    @Test
    public void testLoadVout() throws IOException {
        BitcoindCaller baseImplementation = getBitcoindCallerMock(new Long[]{118398L}, null);
        BitcoindCallerCacheImpl bitcoindCallerCache = new BitcoindCallerCacheImpl(baseImplementation, null);
        when(baseImplementation.loadTransaction(anyString())).thenReturn(null);

        bitcoindCallerCache.getBlock(118398);
        Vout vout = bitcoindCallerCache.getTransactionOut("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033", 0);

        verify(baseImplementation, times(1)).getBlock(118398);
        verify(baseImplementation, never()).loadTransaction(anyString());
        assertEquals("Expecting some transaction",
                30.0d,
                vout.getValue(),
                0.00000001);

    }

    @Test
    public void testLoadVoutTruncation() throws IOException {
        BitcoindCaller baseImplementation = getBitcoindCallerMock(new Long[]{118398L}, null);
        BitcoindCallerCacheImpl bitcoindCallerCache = new BitcoindCallerCacheImpl(baseImplementation, null);
        when(baseImplementation.loadTransaction(anyString())).thenReturn(null);

        GenericResponse<Block> blockRs = bitcoindCallerCache.getBlock(118398);

        Tx testingTx = blockRs.getResult().getTx().stream()
                .filter(
                        o -> o.getTxid().equalsIgnoreCase("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033"))
                .findFirst()
                .orElse(null);
        assertNotNull(testingTx);
        int voutCount = testingTx.getVout().size();
        Vout vout = bitcoindCallerCache.getTransactionOut("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033", 0);


        verify(baseImplementation, times(1)).getBlock(118398);
        verify(baseImplementation, never()).loadTransaction(anyString());
        assertEquals("Expecting some transaction",
                30.0d,
                vout.getValue(),
                0.00000001);
        assertEquals("Expecting transaction truncation",
                voutCount - 1,
                testingTx.getVout().size()
        );

    }

    @Test
    public void testLoadVoutEviction() throws IOException {
        BitcoindCaller baseImplementation = getBitcoindCallerMock(new Long[]{118398L}, null);
        BitcoindCallerCacheImpl bitcoindCallerCache = new BitcoindCallerCacheImpl(baseImplementation, null);
        when(baseImplementation.loadTransaction(anyString())).thenReturn(null);

        GenericResponse<Block> blockRs = bitcoindCallerCache.getBlock(118398);

        Tx testingTx = blockRs.getResult().getTx().stream()
                .filter(
                        o -> o.getTxid().equalsIgnoreCase("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033"))
                .findFirst()
                .orElse(null);
        assertNotNull(testingTx);
        int voutCount = testingTx.getVout().size();
        Vout vout = bitcoindCallerCache.getTransactionOut("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033", 0);
        Vout vout2 = bitcoindCallerCache.getTransactionOut("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033", 1);


        verify(baseImplementation, times(1)).getBlock(118398);
        verify(baseImplementation, never()).loadTransaction(anyString());
        assertEquals("Expecting some transaction",
                30.0d,
                vout.getValue(),
                0.00000001);
        assertEquals("Expecting transaction truncation",
                voutCount - 2,
                testingTx.getVout().size()
        );

        bitcoindCallerCache.loadTransaction("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033");
        //this will mean that transaction was removed from cache
        verify(baseImplementation, times(1)).loadTransaction("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033");
    }

    /**
     * this will test combination of getTransactionOut and loadTransaction
     */
    @Test
    public void testHybridEviction() throws IOException {
        BitcoindCaller baseImplementation = getBitcoindCallerMock(new Long[]{118398L}, null);
        BitcoindCallerCacheImpl bitcoindCallerCache = new BitcoindCallerCacheImpl(baseImplementation, null);
        when(baseImplementation.loadTransaction(anyString())).thenReturn(null);

        GenericResponse<Block> blockRs = bitcoindCallerCache.getBlock(118398);

        Tx testingTx = blockRs.getResult().getTx().stream()
                .filter(
                        o -> o.getTxid().equalsIgnoreCase("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033"))
                .findFirst()
                .orElse(null);
        assertNotNull(testingTx);
        int voutCount = testingTx.getVout().size();
        Vout vout = bitcoindCallerCache.getTransactionOut("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033", 0);
        bitcoindCallerCache.loadTransaction("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033");

        verify(baseImplementation, times(1)).getBlock(118398);
        verify(baseImplementation, never()).loadTransaction(anyString());
        assertEquals("Expecting some transaction",
                30.0d,
                vout.getValue(),
                0.00000001);
        assertEquals("Expecting transaction truncation",
                voutCount - 1,
                testingTx.getVout().size()
        );

        bitcoindCallerCache.loadTransaction("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033");
        //this will mean that transaction was removed from cache
        verify(baseImplementation, times(1)).loadTransaction("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033");
    }

    @Test
    public void testDoubleLoadVoutThrowsError() throws IOException {
        BitcoindCaller baseImplementation = getBitcoindCallerMock(new Long[]{118398L}, null);
        BitcoindCallerCacheImpl bitcoindCallerCache = new BitcoindCallerCacheImpl(baseImplementation, null);
        when(baseImplementation.loadTransaction(anyString())).thenReturn(null);

        GenericResponse<Block> blockRs = bitcoindCallerCache.getBlock(118398);

        Tx testingTx = blockRs.getResult().getTx().stream()
                .filter(
                        o -> o.getTxid().equalsIgnoreCase("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033"))
                .findFirst()
                .orElse(null);
        assertNotNull(testingTx);
        int voutCount = testingTx.getVout().size();
        Vout vout = bitcoindCallerCache.getTransactionOut("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033", 0);
        try {
            Vout vout2 = bitcoindCallerCache.getTransactionOut("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033", 0);
            fail("Loading same output should throw exception");
        } catch (Error e) {
            //this is expected
        }

        verify(baseImplementation, times(1)).getBlock(118398);
        verify(baseImplementation, never()).loadTransaction(anyString());
        assertEquals("Expecting some transaction",
                30.0d,
                vout.getValue(),
                0.00000001);
        assertEquals("Expecting transaction truncation",
                voutCount - 1,
                testingTx.getVout().size()
        );
    }

    @Test
    public void testGetBlockchainSize() throws IOException {
        BitcoindCaller baseImplementation = getBitcoindCallerMock(null, null);
        BitcoindCallerCacheImpl bitcoindCallerCache = new BitcoindCallerCacheImpl(baseImplementation, null);
        when(baseImplementation.getBlockchainSize()).thenReturn(118398L);
        assertEquals("Expected blockchain size returned as is",
                118398L,
                bitcoindCallerCache.getBlockchainSize());
        verify(baseImplementation, times(1)).getBlockchainSize();
    }

    @Test
    public void testLoadVoutReducesCacheSize() throws IOException {
        BitcoindCaller baseImplementation = getBitcoindCallerMock(new Long[]{428097L}, null);
        BitcoindCallerCacheImpl bitcoindCallerCache = new BitcoindCallerCacheImpl(baseImplementation, null);
        when(baseImplementation.loadTransaction(anyString())).thenReturn(null);

        GenericResponse<Block> blockRs = bitcoindCallerCache.getBlock(428097);

        Tx testingTx = blockRs.getResult().getTx().stream()
                .filter(
                        o -> o.getTxid().equalsIgnoreCase("26dba73bd1278d11f1b9f2389b221cf5b84025f6d2ae81e5f8c6c9e312a6e6fa"))
                .findFirst()
                .orElse(null);
        assertNotNull(testingTx);
        int voutCount = testingTx.getVout().size();
        long heapSize = bitcoindCallerCache.getCacheStatistics().getLocalHeapSizeInBytes();
        Vout vout = bitcoindCallerCache.getTransactionOut("26dba73bd1278d11f1b9f2389b221cf5b84025f6d2ae81e5f8c6c9e312a6e6fa", 0);
        long heapSizeAfterRead = bitcoindCallerCache.getCacheStatistics().getLocalHeapSizeInBytes();

        assertTrue("After reading vout cache size should be smaller. Initial=" + heapSize + " now=" + heapSizeAfterRead,
                heapSize > heapSizeAfterRead);

        verify(baseImplementation, times(1)).getBlock(428097);
        verify(baseImplementation, never()).loadTransaction(anyString());
        assertEquals("Expecting transaction truncation",
                voutCount - 1,
                testingTx.getVout().size()
        );
    }

    @Test
    public void testCacheWarmUp() throws IOException {
        BitcoindCaller baseImplementation = getBitcoindCallerMock(new Long[]{118398L,118399L,118400L,118401L,118402L}, null);
        BitcoindCallerCacheImpl bitcoindCallerCache = new BitcoindCallerCacheImpl(baseImplementation, null);

        Map<Long, Block> blocks = new HashMap<>();
        for(long i=118398;i<=118402;i++) {
            blocks.put(i, bitcoindCallerCache.getBlock(i).getResult());
        }
        long localHeapSize = bitcoindCallerCache.getCacheStatistics().getLocalHeapSize();
        for(long i=118398;i<=118402;i++) {
            bitcoindCallerCache.cleanCacheFromBlockInfo(blocks.get(i));
        }
        long cleanedLocalHeapSize = bitcoindCallerCache.getCacheStatistics().getLocalHeapSize();

        assertEquals("There are 45 transactions in mentioned 5 blocks", 45, localHeapSize);
        assertEquals("After cleaning there should be 43 transactions", 43, cleanedLocalHeapSize);

        verify(baseImplementation, never()).loadTransaction(anyString());
    }


    private BitcoindCaller getBitcoindCallerMock(Long[] blocks, String[] transactions) throws IOException {
        BitcoindCaller baseImplementation = mock(BitcoindCaller.class);
        if (blocks != null && blocks.length != 0) {
            for (Long blockNum : blocks) {
                GenericResponse<Block> blockRs = getBlockRs(blockNum);
                when(baseImplementation.getBlock(blockNum)).thenReturn(blockRs);
            }
        } else {
            when(baseImplementation.getBlock(anyLong())).thenReturn(null);
        }
        if (transactions == null || transactions.length == 0) {
            when(baseImplementation.loadTransaction(anyString())).thenReturn(null);
        }
        return baseImplementation;
    }

}
