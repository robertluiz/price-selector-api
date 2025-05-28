package com.example.priceselectorapi.infrastructure.repository.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueryStrategyFactory {

    private final Map<String, PriceQueryStrategy> strategies;

    public PriceQueryStrategy getStrategy(QueryType queryType) {
        log.debug("Getting query strategy for type: {}", queryType);
        
        return Optional.ofNullable(strategies.get(queryType.getStrategyName()))
                .orElseThrow(() -> {
                    log.error("No strategy found for query type: {}", queryType);
                    return new IllegalArgumentException("Unsupported query type: " + queryType);
                });
    }

    public PriceQueryStrategy getDefaultStrategy() {
        log.debug("Getting default query strategy");
        return getStrategy(QueryType.APPLICABLE_PRICE);
    }

    public enum QueryType {
        APPLICABLE_PRICE("applicablePriceQueryStrategy"),
        PRIORITY_BASED("priorityBasedQueryStrategy"),
        DATE_RANGE("dateRangeQueryStrategy");

        private final String strategyName;

        QueryType(String strategyName) {
            this.strategyName = strategyName;
        }

        public String getStrategyName() {
            return strategyName;
        }
    }
} 