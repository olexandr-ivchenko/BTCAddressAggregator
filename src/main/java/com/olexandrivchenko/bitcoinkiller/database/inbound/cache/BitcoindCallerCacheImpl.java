package com.olexandrivchenko.bitcoinkiller.database.inbound.cache;

import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCaller;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Tx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.net.URISyntaxException;

@Service("BitcoindCallerCache")
public class BitcoindCallerCacheImpl implements BitcoindCaller {

    private final static Logger log = LoggerFactory.getLogger(BitcoindCallerCacheImpl.class);

    private BitcoindCaller baseImplementation;
    private Cache<String, CachedTx> txCache;

    private boolean enableCache = true;

    private long entriesAdded = 0;
    private long entriesRead = 0;
    private long entriesDeleted = 0;
    private long transactionsLoaded = 0;

    public BitcoindCallerCacheImpl(@Qualifier("BitcoindCaller") BitcoindCaller baseImplementation) throws URISyntaxException {
        this.baseImplementation = baseImplementation;
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager(
                getClass().getResource("/ehcache.xml").toURI(),
                getClass().getClassLoader());
        txCache = cacheManager.getCache("txCache");

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
                txCache.put(tx.getTxid(), new CachedTx(tx));
            }
            entriesAdded += block.getResult().getTx().size();
            log.debug("Saving block {} transaction. Total {} transaction", number, block.getResult().getTx().size());
        }
        return block;
    }

    @Override
    public Tx loadTransaction(String txid) {
        if (enableCache) {
            CachedTx tx = txCache.get(txid);
            if (tx != null) {
                log.debug("Returning transaction from cache {}", txid);
                entriesRead++;
                tx.notifyRead();
                if(tx.getReadCount() >= tx.getOutCount()) {
                    txCache.remove(txid);
                    entriesDeleted++;
                }else{
                    txCache.put(txid, tx);
                }
                return tx.getTx();
            }
        }
        transactionsLoaded++;
        return baseImplementation.loadTransaction(txid);
    }

    @Scheduled(fixedDelay = 60000)
    public void outputStats() {
        log.info("Added {} transactions, returned from cache {}, and then removed {}, loaded {}", entriesAdded, entriesRead, entriesDeleted, transactionsLoaded);
    }

}
