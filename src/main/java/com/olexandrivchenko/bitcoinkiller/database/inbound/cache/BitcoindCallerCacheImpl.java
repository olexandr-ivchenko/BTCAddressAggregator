package com.olexandrivchenko.bitcoinkiller.database.inbound.cache;

import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCaller;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Tx;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.statistics.StatisticsGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("BitcoindCallerCache")
public class BitcoindCallerCacheImpl implements BitcoindCaller {

    private final static Logger log = LoggerFactory.getLogger(BitcoindCallerCacheImpl.class);

    private BitcoindCaller baseImplementation;
    private LoggingCacheListener cacheLogger;
    private Cache txCache;

    private boolean enableCache = true;

    private long transactionsLoaded = 0;

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
        GenericResponse<Block> block = baseImplementation.getBlock(number);
        if (enableCache) {
            for (Tx tx : block.getResult().getTx()) {
                txCache.put(new Element(tx.getTxid(), new CachedTx(tx)));
            }
            log.debug("Saving block {} transaction. Total {} transaction", number, block.getResult().getTx().size());
        }
        return block;
    }

    @Override
    public Tx loadTransaction(String txid) {
        if (enableCache) {
            Element cacheElement = txCache.get(txid);
            if (cacheElement != null) {
                Object txObj = cacheElement.getObjectValue();
                CachedTx tx = (CachedTx) txObj;
                log.debug("Returning transaction from cache {}", txid);
                tx.notifyRead();
                if (tx.getReadCount() >= tx.getOutCount()) {
                    txCache.remove(txid);
                } else {
                    txCache.put(new Element(txid, tx));
                }
                return tx.getTx();
            }
        }
        transactionsLoaded++;
        return baseImplementation.loadTransaction(txid);
    }

    @Scheduled(fixedDelay = 60000)
    public void outputStats() {
        StatisticsGateway stat = txCache.getStatistics();
        log.info("\nEhcache stats: added={}, removed={}, evicted={}, heapSize={}kb, elementCount={}, hitCount={}, missCount={}, hitRatio={}",
                stat.cachePutAddedCount(),
                stat.cacheRemoveCount(),
                stat.cacheEvictedCount(),
                stat.getLocalHeapSizeInBytes()/1024,
                txCache.getSize(),
                stat.cacheHitCount(),
                stat.cacheMissCount(),
                stat.cacheHitRatio());
//        for (MemoryPoolMXBean mpBean: ManagementFactory.getMemoryPoolMXBeans()) {
//            if (mpBean.getType() == MemoryType.HEAP) {
//                log.debug("Name: {}: {}", mpBean.getName(), mpBean.getUsage()
//                );
//            }
//        }

    }

}
