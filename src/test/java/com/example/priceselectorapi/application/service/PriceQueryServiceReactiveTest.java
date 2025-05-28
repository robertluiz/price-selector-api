package com.example.priceselectorapi.application.service;

import com.example.priceselectorapi.application.cache.CacheKeyGenerator;
import com.example.priceselectorapi.application.cache.CacheStrategy;
import com.example.priceselectorapi.domain.model.Price;
import com.example.priceselectorapi.domain.model.port.PriceRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceQueryServiceReactiveTest {

    @Mock
    private PriceRepositoryPort priceRepositoryPort;

    @Mock
    private CacheStrategy<Object> cacheStrategy;

    @Mock
    private CacheKeyGenerator cacheKeyGenerator;

    @InjectMocks
    private PriceQueryService priceQueryService;

    @Test
    void findApplicablePrice_whenPricesExist_shouldReturnHighestPriorityPrice() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        Long productId = 35455L;
        Integer brandId = 1;
        String cacheKey = "2020-06-14T10:00_35455_1";

        Price price1 = Price.builder()
                .priority(0)
                .priceAmount(new BigDecimal("35.50"))
                .productId(productId)
                .brandId(brandId)
                .build();
        
        Price price2 = Price.builder()
                .priority(1)
                .priceAmount(new BigDecimal("25.45"))
                .productId(productId)
                .brandId(brandId)
                .build();

        when(cacheKeyGenerator.generateKey(applicationDate, productId, brandId))
                .thenReturn(cacheKey);
        when(priceRepositoryPort.findApplicablePrices(applicationDate, productId, brandId))
                .thenReturn(Flux.just(price2, price1));
        when(cacheStrategy.get(eq(cacheKey), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<Mono<Object>> supplier = invocation.getArgument(1);
                    return supplier.get();
                });

        Mono<Price> result = priceQueryService.findApplicablePrice(applicationDate, productId, brandId);

        StepVerifier.create(result)
                .expectNextMatches(price -> 
                    price.getPriceAmount().equals(new BigDecimal("25.45")) && 
                    price.getPriority().equals(1))
                .verifyComplete();

        verify(priceRepositoryPort).findApplicablePrices(applicationDate, productId, brandId);
        verify(cacheKeyGenerator).generateKey(applicationDate, productId, brandId);
        verify(cacheStrategy).get(eq(cacheKey), any(Supplier.class));
    }

    @Test
    void findApplicablePrice_whenNoPricesExist_shouldReturnEmptyMono() {
        LocalDateTime applicationDate = LocalDateTime.of(2021, 1, 1, 10, 0, 0);
        Long productId = 99999L;
        Integer brandId = 99;
        String cacheKey = "2021-01-01T10:00_99999_99";

        when(cacheKeyGenerator.generateKey(applicationDate, productId, brandId))
                .thenReturn(cacheKey);
        when(priceRepositoryPort.findApplicablePrices(applicationDate, productId, brandId))
                .thenReturn(Flux.empty());
        when(cacheStrategy.get(eq(cacheKey), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<Mono<Object>> supplier = invocation.getArgument(1);
                    return supplier.get();
                });

        Mono<Price> result = priceQueryService.findApplicablePrice(applicationDate, productId, brandId);

        StepVerifier.create(result)
                .verifyComplete();

        verify(priceRepositoryPort).findApplicablePrices(applicationDate, productId, brandId);
        verify(cacheKeyGenerator).generateKey(applicationDate, productId, brandId);
        verify(cacheStrategy).get(eq(cacheKey), any(Supplier.class));
    }

    @Test
    void findApplicablePrice_whenSinglePriceExists_shouldReturnThatPrice() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        Long productId = 35455L;
        Integer brandId = 1;
        String cacheKey = "2020-06-14T10:00_35455_1";

        Price singlePrice = Price.builder()
                .priority(0)
                .priceAmount(new BigDecimal("35.50"))
                .productId(productId)
                .brandId(brandId)
                .priceList(1)
                .startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
                .endDate(LocalDateTime.of(2020, 12, 31, 23, 59, 59))
                .curr("EUR")
                .build();

        when(cacheKeyGenerator.generateKey(applicationDate, productId, brandId))
                .thenReturn(cacheKey);
        when(priceRepositoryPort.findApplicablePrices(applicationDate, productId, brandId))
                .thenReturn(Flux.just(singlePrice));
        when(cacheStrategy.get(eq(cacheKey), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<Mono<Object>> supplier = invocation.getArgument(1);
                    return supplier.get();
                });

        Mono<Price> result = priceQueryService.findApplicablePrice(applicationDate, productId, brandId);

        StepVerifier.create(result)
                .expectNextMatches(price -> 
                    price.getPriceAmount().equals(new BigDecimal("35.50")) &&
                    price.getProductId().equals(productId) &&
                    price.getBrandId().equals(brandId) &&
                    price.getCurr().equals("EUR"))
                .verifyComplete();

        verify(priceRepositoryPort).findApplicablePrices(applicationDate, productId, brandId);
        verify(cacheKeyGenerator).generateKey(applicationDate, productId, brandId);
        verify(cacheStrategy).get(eq(cacheKey), any(Supplier.class));
    }
} 