package com.example.priceselectorapi.application.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactiveCacheService implements CacheStrategy<Object> {

    private final CacheManager cacheManager;
    private static final String CACHE_NAME = "prices";

    @Override
    public Mono<Object> get(String key, Supplier<Mono<Object>> valueSupplier) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            log.warn("Cache '{}' not found, executing supplier directly", CACHE_NAME);
            return valueSupplier.get();
        }

        Cache.ValueWrapper cachedValue = cache.get(key);
        if (cachedValue != null) {
            log.debug("Cache hit for key: {}", key);
            return Mono.justOrEmpty(cachedValue.get());
        }

        log.debug("Cache miss for key: {}", key);
        return valueSupplier.get()
                .doOnNext(value -> {
                    if (value != null) {
                        cache.put(key, value);
                        log.debug("Cached value for key: {}", key);
                    }
                });
    }

    @Override
    public void evict(String key) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.evict(key);
            log.debug("Evicted cache entry for key: {}", key);
        }
    }

    @Override
    public void clear() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.clear();
            log.debug("Cleared cache: {}", CACHE_NAME);
        }
    }
} 