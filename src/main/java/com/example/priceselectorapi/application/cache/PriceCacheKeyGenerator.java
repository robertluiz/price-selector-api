package com.example.priceselectorapi.application.cache;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PriceCacheKeyGenerator implements CacheKeyGenerator {

    @Override
    public String generateKey(LocalDateTime applicationDate, Long productId, Integer brandId) {
        return String.format("%s_%d_%d", 
            applicationDate.toString(), 
            productId, 
            brandId);
    }
} 