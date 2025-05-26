package com.example.priceselectorapi.domain.port;

import com.example.priceselectorapi.domain.model.Price;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceRepositoryPort {
    
    List<Price> findApplicablePrices(LocalDateTime applicationDate, Long productId, Integer brandId);
} 