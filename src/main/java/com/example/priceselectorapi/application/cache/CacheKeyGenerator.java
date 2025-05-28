package com.example.priceselectorapi.application.cache;

import java.time.LocalDateTime;

public interface CacheKeyGenerator {
    
    String generateKey(LocalDateTime applicationDate, Long productId, Integer brandId);
} 