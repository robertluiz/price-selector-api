package com.example.priceselectorapi.domain.model.port;

import com.example.priceselectorapi.domain.model.Price;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface PriceRepositoryPort {
    
    Flux<Price> findApplicablePrices(LocalDateTime applicationDate, Long productId, Integer brandId);
} 