package com.example.priceselectorapi.domain.model.factory;

import com.example.priceselectorapi.domain.model.Price;
import com.example.priceselectorapi.domain.model.valueobject.DateRange;
import com.example.priceselectorapi.domain.model.valueobject.Money;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@UtilityClass
@Slf4j
public class PriceFactory {

    public static Price createPrice(Long id, Integer brandId, Integer priceList, 
                                  Long productId, Integer priority, 
                                  LocalDateTime startDate, LocalDateTime endDate,
                                  BigDecimal amount, String currencyCode) {
        
        log.debug("Creating Price entity for productId: {}, brandId: {}", productId, brandId);
        
        validatePriceCreationData(brandId, priceList, productId, priority, 
                                startDate, endDate, amount, currencyCode);
        
        DateRange dateRange = DateRange.of(startDate, endDate);
        Money money = Money.of(amount, currencyCode);
        
        return Price.builder()
                .id(id)
                .brandId(brandId)
                .priceList(priceList)
                .productId(productId)
                .priority(priority)
                .validityPeriod(dateRange)
                .price(money)
                .startDate(startDate)
                .endDate(endDate)
                .priceAmount(amount)
                .curr(currencyCode)
                .build();
    }
    
    public static Price createPriceWithValueObjects(Long id, Integer brandId, Integer priceList, 
                                                  Long productId, Integer priority, 
                                                  DateRange dateRange, Money money) {
        
        log.debug("Creating Price entity with Value Objects for productId: {}, brandId: {}", 
                productId, brandId);
        
        validateValueObjects(dateRange, money);
        validateBasicData(brandId, priceList, productId, priority);
        
        return Price.builder()
                .id(id)
                .brandId(brandId)
                .priceList(priceList)
                .productId(productId)
                .priority(priority)
                .validityPeriod(dateRange)
                .price(money)
                .startDate(dateRange.getStartDate())
                .endDate(dateRange.getEndDate())
                .priceAmount(money.getAmount())
                .curr(money.getCurrencyCode())
                .build();
    }
    
    private static void validatePriceCreationData(Integer brandId, Integer priceList, 
                                                Long productId, Integer priority,
                                                LocalDateTime startDate, LocalDateTime endDate,
                                                BigDecimal amount, String currencyCode) {
        
        validateBasicData(brandId, priceList, productId, priority);
        
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price amount cannot be null or negative");
        }
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency code cannot be null or empty");
        }
    }
    
    private static void validateBasicData(Integer brandId, Integer priceList, 
                                        Long productId, Integer priority) {
        if (brandId == null || brandId <= 0) {
            throw new IllegalArgumentException("Brand ID must be positive");
        }
        if (priceList == null || priceList <= 0) {
            throw new IllegalArgumentException("Price list must be positive");
        }
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive");
        }
        if (priority == null || priority < 0) {
            throw new IllegalArgumentException("Priority cannot be negative");
        }
    }
    
    private static void validateValueObjects(DateRange dateRange, Money money) {
        if (dateRange == null) {
            throw new IllegalArgumentException("DateRange cannot be null");
        }
        if (money == null) {
            throw new IllegalArgumentException("Money cannot be null");
        }
    }
} 