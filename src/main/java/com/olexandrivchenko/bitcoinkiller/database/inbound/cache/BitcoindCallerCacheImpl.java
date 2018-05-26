package com.olexandrivchenko.bitcoinkiller.database.inbound.cache;

import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCaller;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Tx;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Vout;
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
        if (enableCache) {
            Element cacheElement = txCache.get(txid);
            if (cacheElement != null) {
                Object txObj = cacheElement.getObjectValue();
                CachedTx tx = (CachedTx) txObj;
                log.debug("Returning transaction from cache {}", txid);
                if(notifyRead) {
                    tx.notifyRead();
                }
                if (tx.getReadCount() >= tx.getOutCount()) {
                    txCache.remove(txid);
                }
                return tx.getTx();
            }
        }
        return baseImplementation.loadTransaction(txid);
    }

    @Override
    public Vout getTransactionOut(String txid, Integer n) {
        Tx transaction = loadTransactionInternal(txid, false);
        for(int i=0;i<transaction.getVout().size();i++){
            Vout vout = transaction.getVout().get(i);
            if (Integer.compare(vout.getN(), n) == 0) {
                transaction.getVout().remove(i);
                if(transaction.getVout().size() == 0){
                    txCache.remove(txid);
                }else{
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

    private void notifyCacheSizeChange(String txid){
        if (enableCache) {
            Element cacheElement = txCache.get(txid);
            if (cacheElement != null) {
                txCache.put(cacheElement);
            }
        }

    }

    public StatisticsGateway getCacheStatistics(){
        return txCache.getStatistics();
    }

    @Scheduled(fixedDelay = 60000)
    public void outputStats() {
        StatisticsGateway stat = txCache.getStatistics();
        log.info("\nEhcache stats: added={}, removed={}, evicted={}, heapSize={}kb, elementCount={}, hitCount={}, missCount={}, hitRatio={}, lastBlock={}",
                stat.cachePutAddedCount(),
                stat.cacheRemoveCount(),
                stat.cacheEvictedCount(),
                stat.getLocalHeapSizeInBytes() / 1024,
                txCache.getSize(),
                stat.cacheHitCount(),
                stat.cacheMissCount(),
                new DecimalFormat("###,###.00").format((double)stat.cacheHitCount()/stat.cacheMissCount()),
                lastRequestedBlock);
//        for (MemoryPoolMXBean mpBean: ManagementFactory.getMemoryPoolMXBeans()) {
//            if (mpBean.getType() == MemoryType.HEAP) {
//                log.debug("Name: {}: {}", mpBean.getName(), mpBean.getUsage()
//                );
//            }
//        }

    }

}
