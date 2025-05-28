package com.example.priceselectorapi.domain.model.port;

import com.example.priceselectorapi.domain.model.Price;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface PriceQueryPort {
    
    Mono<Price> findApplicablePrice(LocalDateTime applicationDate, Long productId, Integer brandId);
} 