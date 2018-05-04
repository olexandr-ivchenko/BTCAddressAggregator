package com.olexandrivchenko.bitcoinkiller.database.inbound.cache;

import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCaller;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Tx;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;

@Service("BitcoindCallerCache")
public class BitcoindCallerCacheImpl implements BitcoindCaller {

    private final static Logger log = LoggerFactory.getLogger(BitcoindCallerCacheImpl.class);

    private BitcoindCaller baseImplementation;
    private LoggingCacheListener cacheLogger;
    private Cache txCache;

    private boolean enableCache = true;

    private long entriesAdded = 0;
    private long entriesRead = 0;
    private long entriesDeleted = 0;
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
            entriesAdded += block.getResult().getTx().size();
            log.debug("Saving block {} transaction. Total {} transaction", number, block.getResult().getTx().size());
        }
        return block;
    }

    @Override
    public Tx loadTransaction(String txid) {
        if (enableCache) {
            Object txObj = ofNullable(txCache.get(txid)).map(Element::getObjectValue).orElse(null);
            if (txObj != null) {
                CachedTx tx = (CachedTx) txObj;
                log.debug("Returning transaction from cache {}", txid);
                entriesRead++;
                tx.notifyRead();
                if (tx.getReadCount() >= tx.getOutCount()) {
                    txCache.remove(txid);
                    entriesDeleted++;
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
        log.info("Added {} transactions, returned from cache {}, and then removed {}, loaded {}, evicted {}",
                entriesAdded, entriesRead, entriesDeleted, transactionsLoaded, cacheLogger.getEvictedCount());
    }

}
