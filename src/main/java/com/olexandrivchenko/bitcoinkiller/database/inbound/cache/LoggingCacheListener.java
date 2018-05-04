package com.olexandrivchenko.bitcoinkiller.database.inbound.cache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingCacheListener implements CacheEventListener {

    private final static Logger log = LoggerFactory.getLogger(LoggingCacheListener.class);

    private long evictedCount = 0;

    @Override
    public void notifyElementRemoved(Ehcache ehcache, Element element) throws CacheException {

    }

    @Override
    public void notifyElementPut(Ehcache ehcache, Element element) throws CacheException {
//        log.debug("ADDED {}", element.getObjectKey());
    }

    @Override
    public void notifyElementUpdated(Ehcache ehcache, Element element) throws CacheException {

    }

    @Override
    public void notifyElementExpired(Ehcache ehcache, Element element) {

    }

    @Override
    public void notifyElementEvicted(Ehcache ehcache, Element element) {
        log.debug("EVICTED {}", element.getObjectKey());
        evictedCount++;
    }

    @Override
    public void notifyRemoveAll(Ehcache ehcache) {

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public void dispose() {

    }

    public long getEvictedCount() {
        return evictedCount;
    }
}
