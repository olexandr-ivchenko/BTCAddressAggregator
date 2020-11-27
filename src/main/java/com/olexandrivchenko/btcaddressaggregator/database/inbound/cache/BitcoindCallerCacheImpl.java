package com.olexandrivchenko.btcaddressaggregator.database.inbound.cache;

import com.olexandrivchenko.btcaddressaggregator.database.inbound.BitcoindCaller;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.statistics.StatisticsGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;

@Service("BitcoindCallerCache")
public class BitcoindCallerCacheImpl implements BitcoindCaller {

    private final static Logger log = LoggerFactory.getLogger(BitcoindCallerCacheImpl.class);

    private BitcoindCaller baseImplementation;
    private LoggingCacheListener cacheLogger;
    private Cache txCache;

    private boolean enableCache = true;
    private boolean aggressiveCacheTrim = true;

    private long lastRequestedBlock = 0;

    public BitcoindCallerCacheImpl(@Qualifier("BitcoindCaller") BitcoindCaller baseImplementation,
                                   LoggingCacheListener cacheLogger) {
        this.baseImplementation = baseImplementation;
        this.cacheLogger = cacheLogger;
        CacheManager cacheManager = CacheManager.getInstance();
        this.txCache = cacheManager.getCache("txCache");
        this.txCache.getCacheEventNotificationService().registerListener(this.cacheLogger);

    }

    @Override
    public long getBlockchainSize() {
        return baseImplementation.getBlockchainSize();
    }

    @Override
    public GenericResponse<Block> getBlock(long number) {
        lastRequestedBlock = number;
        GenericResponse<Block> block = baseImplementation.getBlock(number);
        if (enableCache) {
            for (Tx tx : block.getResult().getTx()) {
                if (aggressiveCacheTrim) {
                    cutUnusedFields(tx);
                }
                txCache.put(new Element(tx.getTxid(), new CachedTx(tx)));
            }
            log.debug("Saving block {} transaction. Total {} transaction", number, block.getResult().getTx().size());
        }
        return block;
    }

    private void cutUnusedFields(Tx tx) {
        tx.setHash(null);
        tx.setHex(null);
        tx.getVin().forEach(o -> {
            if (o.getScriptSig() != null) {
                o.getScriptSig().setAsm(null);
                o.getScriptSig().setHex(null);
            }
        });
        tx.getVout().forEach(o -> {
            if (o.getScriptPubKey() != null) {
                o.getScriptPubKey().setAsm(null);
                o.getScriptPubKey().setHex(null);
            }
        });
    }

    @Override
    public Tx loadTransaction(String txid) {
        return loadTransactionInternal(txid, true);
    }

    private Tx loadTransactionInternal(String txid, boolean notifyRead) {
        Tx transactionFromCache = getTransactionFromCache(txid, notifyRead);
        if (transactionFromCache != null) {
            return transactionFromCache;
        }
        return baseImplementation.loadTransaction(txid);
    }

    private Tx getTransactionFromCache(String txid, boolean notifyRead) {
        if (enableCache) {
            Element cacheElement = txCache.get(txid);
            if (cacheElement != null) {
                Object txObj = cacheElement.getObjectValue();
                CachedTx tx = (CachedTx) txObj;
                log.debug("Returning transaction from cache {}", txid);
                if (notifyRead) {
                    tx.notifyRead();
                }
                if (tx.getReadCount() >= tx.getOutCount()) {
                    txCache.remove(txid);
                }
                return tx.getTx();
            }
        }
        return null;
    }

    @Override
    public Vout getTransactionOut(String txid, Integer n) {
        return getTransactionOut(txid, n, true);
    }

    private Vout getTransactionOut(String txid, Integer n, boolean loadTransactionFromBlockchain) {
        Tx transaction;
        if (loadTransactionFromBlockchain) {
            transaction = loadTransactionInternal(txid, false);
        } else {
            transaction = getTransactionFromCache(txid, false);
            if (transaction == null) {
                return null;
            }
        }
        for (int i = 0; i < transaction.getVout().size(); i++) {
            Vout vout = transaction.getVout().get(i);
            if (Integer.compare(vout.getN(), n) == 0) {
                transaction.getVout().remove(i);
                if (transaction.getVout().size() == 0) {
                    txCache.remove(txid);
                } else {
                    notifyCacheSizeChange(txid);
                }
                return vout;
            }

        }
        throw new Error("This is bad code! " +
                "Requested transaction " + transaction.getTxid() +
                " input number " + n +
                " from total left " + transaction.getVout().size());
    }

    public void cleanCacheFromBlockInfo(Block block) {

        for (Tx tx : block.getTx()) {
            List<Vin> vin = tx.getVin();
            tx.setVin(null);
            if (!isBlockReward(vin)) {
                for (Vin vinn : vin) {
                    getTransactionOut(vinn.getTxid(), vinn.getVout(), false);
                }

            }
        }
    }

    private boolean isBlockReward(List<Vin> vin) {
        return vin.size() == 1
                && vin.get(0).getCoinbase() != null
                && vin.get(0).getTxid() == null
                && vin.get(0).getScriptSig() == null;
    }


    private void notifyCacheSizeChange(String txid) {
        if (enableCache) {
            Element cacheElement = txCache.get(txid);
            if (cacheElement != null) {
                txCache.put(cacheElement);
            }
        }

    }

    public StatisticsGateway getCacheStatistics() {
        return txCache.getStatistics();
    }

    private long lastAdded = 0;
    private long lastRemoved = 0;
    private long lastEvicted = 0;
    private long lastHeapSize = 0;
    private long lastElementCount = 0;
    private long lastHitCount = 0;
    private long lastMissCount = 0;
    private long lastProcessedBlock = 0;

    @Scheduled(fixedDelay = 60000)
    public void outputStats() {
        StatisticsGateway stat = txCache.getStatistics();
        long cachePutAddedCount = stat.cachePutAddedCount();
        long cacheRemoveCount = stat.cacheRemoveCount();
        long cacheEvictedCount = stat.cacheEvictedCount();
        long localHeapSizeInBytes = stat.getLocalHeapSizeInBytes();
        int size = txCache.getSize();
        long cacheHitCount = stat.cacheHitCount();
        long cacheMissCount = stat.cacheMissCount();
        log.info("\nEhcache stats: added={}, removed={}, evicted={}, heapSize={}kb, elementCount={}, hitCount={}, missCount={}, hitRatio={}, lastBlock={}" +
                        "\nLast stats: added={}, removed={}, evicted={}, heapSize={}kb, elementCount={}, hitCount={}, missCount={}, hitRatio={}, processedBlocks={}",
                cachePutAddedCount,
                cacheRemoveCount,
                cacheEvictedCount,
                localHeapSizeInBytes / 1024,
                size,
                cacheHitCount,
                cacheMissCount,
                new DecimalFormat("###,###.00").format((double) cacheHitCount / (cacheMissCount + 1)),
                lastRequestedBlock,
                cachePutAddedCount - lastAdded,
                cacheRemoveCount - lastRemoved,
                cacheEvictedCount - lastEvicted,
                (localHeapSizeInBytes - lastHeapSize) / 1024,
                size - lastElementCount,
                cacheHitCount - lastHitCount,
                cacheMissCount - lastMissCount,
                new DecimalFormat("###,###.00").format((double) (cacheHitCount - lastHitCount) / (cacheMissCount - lastMissCount + 1)),
                lastRequestedBlock - lastProcessedBlock);
        lastAdded = cachePutAddedCount;
        lastRemoved = cacheRemoveCount;
        lastEvicted = cacheEvictedCount;
        lastHeapSize = localHeapSizeInBytes;
        lastElementCount = size;
        lastHitCount = cacheHitCount;
        lastMissCount = cacheMissCount;
        lastProcessedBlock = lastRequestedBlock;
    }

}
