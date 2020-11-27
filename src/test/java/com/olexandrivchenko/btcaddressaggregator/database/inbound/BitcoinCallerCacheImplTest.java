package com.olexandrivchenko.btcaddressaggregator.database.inbound;

import com.olexandrivchenko.btcaddressaggregator.database.inbound.cache.BitcoindCallerCacheImpl;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc.Tx;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc.Vout;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.olexandrivchenko.btcaddressaggregator.database.tools.TestingUtils.getBlockRs;
import static org.junit.Assert.assertEquals;

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
        Mockito.verify(baseImplementation, Mockito.never()).loadTransaction(Matchers.anyString());
    }

    @Test
    public void testLoadTransactionEviction() throws IOException {
        BitcoindCaller baseImplementation = getBitcoindCallerMock(new Long[]{118398L}, null);
        BitcoindCallerCacheImpl bitcoindCallerCache = new BitcoindCallerCacheImpl(baseImplementation, null);
        Mockito.when(baseImplementation.loadTransaction(Matchers.anyString())).thenReturn(null);

        bitcoindCallerCache.getBlock(118398);
        Tx tx = bitcoindCallerCache.loadTransaction("5f0cf1cb26b2a92a5b9f22b9b20abd7ec9a6046fa8f9c6f05c0cfcf85b5c7ce7");
        bitcoindCallerCache.loadTransaction("5f0cf1cb26b2a92a5b9f22b9b20abd7ec9a6046fa8f9c6f05c0cfcf85b5c7ce7");

        Mockito.verify(baseImplementation, Mockito.times(1)).getBlock(118398);
        Mockito.verify(baseImplementation, Mockito.times(1)).loadTransaction("5f0cf1cb26b2a92a5b9f22b9b20abd7ec9a6046fa8f9c6f05c0cfcf85b5c7ce7");
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
        Mockito.when(baseImplementation.loadTransaction(Matchers.anyString())).thenReturn(null);

        bitcoindCallerCache.getBlock(118398);
        Vout vout = bitcoindCallerCache.getTransactionOut("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033", 0);

        Mockito.verify(baseImplementation, Mockito.times(1)).getBlock(118398);
        Mockito.verify(baseImplementation, Mockito.never()).loadTransaction(Matchers.anyString());
        assertEquals("Expecting some transaction",
                30.0d,
                vout.getValue(),
                0.00000001);

    }

    @Test
    public void testLoadVoutTruncation() throws IOException {
        BitcoindCaller baseImplementation = getBitcoindCallerMock(new Long[]{118398L}, null);
        BitcoindCallerCacheImpl bitcoindCallerCache = new BitcoindCallerCacheImpl(baseImplementation, null);
        Mockito.when(baseImplementation.loadTransaction(Matchers.anyString())).thenReturn(null);

        GenericResponse<Block> blockRs = bitcoindCallerCache.getBlock(118398);

        Tx testingTx = blockRs.getResult().getTx().stream()
                .filter(
                        o -> o.getTxid().equalsIgnoreCase("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033"))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(testingTx);
        int voutCount = testingTx.getVout().size();
        Vout vout = bitcoindCallerCache.getTransactionOut("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033", 0);


        Mockito.verify(baseImplementation, Mockito.times(1)).getBlock(118398);
        Mockito.verify(baseImplementation, Mockito.never()).loadTransaction(Matchers.anyString());
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
        Mockito.when(baseImplementation.loadTransaction(Matchers.anyString())).thenReturn(null);

        GenericResponse<Block> blockRs = bitcoindCallerCache.getBlock(118398);

        Tx testingTx = blockRs.getResult().getTx().stream()
                .filter(
                        o -> o.getTxid().equalsIgnoreCase("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033"))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(testingTx);
        int voutCount = testingTx.getVout().size();
        Vout vout = bitcoindCallerCache.getTransactionOut("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033", 0);
        Vout vout2 = bitcoindCallerCache.getTransactionOut("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033", 1);


        Mockito.verify(baseImplementation, Mockito.times(1)).getBlock(118398);
        Mockito.verify(baseImplementation, Mockito.never()).loadTransaction(Matchers.anyString());
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
        Mockito.verify(baseImplementation, Mockito.times(1)).loadTransaction("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033");
    }

    /**
     * this will test combination of getTransactionOut and loadTransaction
     */
    @Test
    public void testHybridEviction() throws IOException {
        BitcoindCaller baseImplementation = getBitcoindCallerMock(new Long[]{118398L}, null);
        BitcoindCallerCacheImpl bitcoindCallerCache = new BitcoindCallerCacheImpl(baseImplementation, null);
        Mockito.when(baseImplementation.loadTransaction(Matchers.anyString())).thenReturn(null);

        GenericResponse<Block> blockRs = bitcoindCallerCache.getBlock(118398);

        Tx testingTx = blockRs.getResult().getTx().stream()
                .filter(
                        o -> o.getTxid().equalsIgnoreCase("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033"))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(testingTx);
        int voutCount = testingTx.getVout().size();
        Vout vout = bitcoindCallerCache.getTransactionOut("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033", 0);
        bitcoindCallerCache.loadTransaction("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033");

        Mockito.verify(baseImplementation, Mockito.times(1)).getBlock(118398);
        Mockito.verify(baseImplementation, Mockito.never()).loadTransaction(Matchers.anyString());
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
        Mockito.verify(baseImplementation, Mockito.times(1)).loadTransaction("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033");
    }

    @Test
    public void testDoubleLoadVoutThrowsError() throws IOException {
        BitcoindCaller baseImplementation = getBitcoindCallerMock(new Long[]{118398L}, null);
        BitcoindCallerCacheImpl bitcoindCallerCache = new BitcoindCallerCacheImpl(baseImplementation, null);
        Mockito.when(baseImplementation.loadTransaction(Matchers.anyString())).thenReturn(null);

        GenericResponse<Block> blockRs = bitcoindCallerCache.getBlock(118398);

        Tx testingTx = blockRs.getResult().getTx().stream()
                .filter(
                        o -> o.getTxid().equalsIgnoreCase("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033"))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(testingTx);
        int voutCount = testingTx.getVout().size();
        Vout vout = bitcoindCallerCache.getTransactionOut("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033", 0);
        try {
            Vout vout2 = bitcoindCallerCache.getTransactionOut("463866355098cf2b3362812dfe6b236bdaf96d07af3f8deeb083a131dbe2e033", 0);
            Assert.fail("Loading same output should throw exception");
        } catch (Error e) {
            //this is expected
        }

        Mockito.verify(baseImplementation, Mockito.times(1)).getBlock(118398);
        Mockito.verify(baseImplementation, Mockito.never()).loadTransaction(Matchers.anyString());
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
        Mockito.when(baseImplementation.getBlockchainSize()).thenReturn(118398L);
        assertEquals("Expected blockchain size returned as is",
                118398L,
                bitcoindCallerCache.getBlockchainSize());
        Mockito.verify(baseImplementation, Mockito.times(1)).getBlockchainSize();
    }

    @Test
    public void testLoadVoutReducesCacheSize() throws IOException {
        BitcoindCaller baseImplementation = getBitcoindCallerMock(new Long[]{428097L}, null);
        BitcoindCallerCacheImpl bitcoindCallerCache = new BitcoindCallerCacheImpl(baseImplementation, null);
        Mockito.when(baseImplementation.loadTransaction(Matchers.anyString())).thenReturn(null);

        GenericResponse<Block> blockRs = bitcoindCallerCache.getBlock(428097);

        Tx testingTx = blockRs.getResult().getTx().stream()
                .filter(
                        o -> o.getTxid().equalsIgnoreCase("26dba73bd1278d11f1b9f2389b221cf5b84025f6d2ae81e5f8c6c9e312a6e6fa"))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(testingTx);
        int voutCount = testingTx.getVout().size();
        long heapSize = bitcoindCallerCache.getCacheStatistics().getLocalHeapSizeInBytes();
        Vout vout = bitcoindCallerCache.getTransactionOut("26dba73bd1278d11f1b9f2389b221cf5b84025f6d2ae81e5f8c6c9e312a6e6fa", 0);
        long heapSizeAfterRead = bitcoindCallerCache.getCacheStatistics().getLocalHeapSizeInBytes();

        Assert.assertTrue("After reading vout cache size should be smaller. Initial=" + heapSize + " now=" + heapSizeAfterRead,
                heapSize > heapSizeAfterRead);

        Mockito.verify(baseImplementation, Mockito.times(1)).getBlock(428097);
        Mockito.verify(baseImplementation, Mockito.never()).loadTransaction(Matchers.anyString());
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

        Assert.assertEquals("There are 45 transactions in mentioned 5 blocks", 45, localHeapSize);
        Assert.assertEquals("After cleaning there should be 43 transactions", 43, cleanedLocalHeapSize);

        Mockito.verify(baseImplementation, Mockito.never()).loadTransaction(Matchers.anyString());
    }


    private BitcoindCaller getBitcoindCallerMock(Long[] blocks, String[] transactions) throws IOException {
        BitcoindCaller baseImplementation = Mockito.mock(BitcoindCaller.class);
        if (blocks != null && blocks.length != 0) {
            for (Long blockNum : blocks) {
                GenericResponse<Block> blockRs = getBlockRs(blockNum);
                Mockito.when(baseImplementation.getBlock(blockNum)).thenReturn(blockRs);
            }
        } else {
            Mockito.when(baseImplementation.getBlock(Matchers.anyLong())).thenReturn(null);
        }
        if (transactions == null || transactions.length == 0) {
            Mockito.when(baseImplementation.loadTransaction(Matchers.anyString())).thenReturn(null);
        }
        return baseImplementation;
    }

}
