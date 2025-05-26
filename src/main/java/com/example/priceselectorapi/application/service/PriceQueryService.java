package com.example.priceselectorapi.application.service;

import com.example.priceselectorapi.domain.model.Price;
import com.example.priceselectorapi.domain.port.PriceQueryPort;
import com.example.priceselectorapi.domain.port.PriceRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceQueryService implements PriceQueryPort {

    private final PriceRepositoryPort priceRepositoryPort;

    /**
     * Finds the applicable price for a given product, brand, and application date.
     * If multiple prices are valid for the given date, the one with the highest priority is returned.
     * Uses reactive programming for non-blocking operations and caching for performance.
     *
     * @param applicationDate The date and time for which the price is requested.
     * @param productId The ID of the product.
     * @param brandId The ID of the brand.
     * @return A Mono containing the applicable Price if found, otherwise an empty Mono.
     */
    @Cacheable(value = "prices", key = "#applicationDate.toString() + '_' + #productId + '_' + #brandId")
    public Mono<Price> findApplicablePrice(
            LocalDateTime applicationDate,
            Long productId,
            Integer brandId) {

        return Mono.fromCallable(() -> {
                    log.debug("Searching for applicable prices for productId: {}, brandId: {}, date: {}", 
                        productId, brandId, applicationDate);
                    
                    return priceRepositoryPort.findApplicablePrices(applicationDate, productId, brandId);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .doOnNext(price -> log.debug("Found applicable price with priority: {} and amount: {}", 
                    price.getPriority(), price.getPriceAmount()))
                .next()
                .doOnSuccess(price -> {
                    if (price == null) {
                        log.debug("No applicable price found");
                    }
                });
    }
} 