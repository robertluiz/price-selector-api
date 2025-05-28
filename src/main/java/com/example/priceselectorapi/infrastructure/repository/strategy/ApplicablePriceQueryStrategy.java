package com.example.priceselectorapi.infrastructure.repository.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicablePriceQueryStrategy implements PriceQueryStrategy {

    private final Map<String, String> sqlQueries;

    @Override
    public String getQuery() {
        log.debug("Getting applicable price query");
        return sqlQueries.get("findApplicablePrices");
    }

    @Override
    public Map<String, Object> getParameters(LocalDateTime applicationDate, Long productId, Integer brandId) {
        log.debug("Creating query parameters for productId: {}, brandId: {}, date: {}", 
                 productId, brandId, applicationDate);
        
        return Map.of(
            "applicationDate", applicationDate,
            "productId", productId,
            "brandId", brandId
        );
    }

    @Override
    public String getStrategyName() {
        return "applicablePriceQueryStrategy";
    }
} 