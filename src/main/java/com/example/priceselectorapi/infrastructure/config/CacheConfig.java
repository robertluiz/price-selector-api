package com.example.priceselectorapi.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.prices.ttl:PT5M}")
    private Duration pricesCacheTtl;

    @Value("${app.cache.prices.max-size:1000}")
    private long pricesCacheMaxSize;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeineCacheBuilder());
        cacheManager.setCacheNames(java.util.Set.of("prices"));
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(pricesCacheMaxSize)
                .expireAfterWrite(pricesCacheTtl)
                .recordStats();
    }
} 