package com.example.priceselectorapi.infrastructure.repository.strategy;

import java.time.LocalDateTime;
import java.util.Map;

public interface PriceQueryStrategy {
    
    String getQuery();
    
    Map<String, Object> getParameters(LocalDateTime applicationDate, Long productId, Integer brandId);
    
    String getStrategyName();
} 