package com.example.priceselectorapi.infrastructure.repository;

import com.example.priceselectorapi.domain.model.Price;
import com.example.priceselectorapi.domain.model.port.PriceRepositoryPort;
import com.example.priceselectorapi.infrastructure.repository.mapper.PriceRowMapper;
import com.example.priceselectorapi.infrastructure.repository.strategy.PriceQueryStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PriceRepositoryImpl implements PriceRepositoryPort {

    private final DatabaseClient databaseClient;
    private final PriceRowMapper priceRowMapper;
    private final PriceQueryStrategy queryStrategy;

    @Override
    public Flux<Price> findApplicablePrices(LocalDateTime applicationDate, Long productId, Integer brandId) {
        log.debug("Finding applicable prices using strategy: {}", queryStrategy.getStrategyName());
        
        String query = queryStrategy.getQuery();
        Map<String, Object> parameters = queryStrategy.getParameters(applicationDate, productId, brandId);
        
        log.debug("Executing query for productId: {}, brandId: {}, date: {}", 
                 productId, brandId, applicationDate);
        
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(query);
        
        for (Map.Entry<String, Object> param : parameters.entrySet()) {
            executeSpec = executeSpec.bind(param.getKey(), param.getValue());
        }
        
        return executeSpec
                .map(priceRowMapper::mapRowToPrice)
                .all()
                .doOnNext(price -> log.debug("Mapped price: {} for product: {}", 
                    price.getId(), price.getProductId()))
                .doOnComplete(() -> log.debug("Completed finding applicable prices"))
                .doOnError(error -> log.error("Error finding applicable prices", error));
    }
} 