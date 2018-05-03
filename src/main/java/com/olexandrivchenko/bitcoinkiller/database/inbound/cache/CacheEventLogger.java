package com.olexandrivchenko.bitcoinkiller.database.inbound.cache;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheEventLogger implements CacheEventListener<Object, Object> {

    private final static Logger log = LoggerFactory.getLogger(CacheEventLogger.class);

    @Override
    public void onEvent(CacheEvent<?, ?> cacheEvent) {
        log.debug("Cache event {} for key {}", cacheEvent.getType().toString(), cacheEvent.getKey());
    }
}
