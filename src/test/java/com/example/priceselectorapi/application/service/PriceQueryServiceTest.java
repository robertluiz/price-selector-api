package com.example.priceselectorapi.application.service;

import com.example.priceselectorapi.domain.model.Price;
import com.example.priceselectorapi.domain.repository.PriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceQueryServiceTest {

    @Mock
    private PriceRepository priceRepository;

    @InjectMocks
    private PriceQueryService priceQueryService;

    @Test
    void findApplicablePrice_whenPricesExist_shouldReturnHighestPriorityPrice() {
        // Arrange
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        Long productId = 35455L;
        Integer brandId = 1;

        Price price1 = Price.builder().priority(0).priceAmount(new BigDecimal("35.50")).build(); // Lower priority
        Price price2 = Price.builder().priority(1).priceAmount(new BigDecimal("25.45")).build(); // Higher priority

        List<Price> mockPrices = Arrays.asList(price2, price1); // Repo returns them ordered by priority DESC

        when(priceRepository.findApplicablePrices(applicationDate, productId, brandId))
                .thenReturn(mockPrices);

        // Act
        Optional<Price> result = priceQueryService.findApplicablePrice(applicationDate, productId, brandId);

        // Assert
        assertTrue(result.isPresent(), "Price should be found");
        assertEquals(price2.getPriceAmount(), result.get().getPriceAmount(), "Should return the price with highest priority");
        assertEquals(1, result.get().getPriority(), "Priority should be 1");
        verify(priceRepository).findApplicablePrices(applicationDate, productId, brandId);
    }

    @Test
    void findApplicablePrice_whenNoPricesExist_shouldReturnEmptyOptional() {
        // Arrange
        LocalDateTime applicationDate = LocalDateTime.of(2021, 1, 1, 10, 0, 0);
        Long productId = 99999L;
        Integer brandId = 99;

        when(priceRepository.findApplicablePrices(applicationDate, productId, brandId))
                .thenReturn(Collections.emptyList());

        // Act
        Optional<Price> result = priceQueryService.findApplicablePrice(applicationDate, productId, brandId);

        // Assert
        assertFalse(result.isPresent(), "Price should not be found");
        verify(priceRepository).findApplicablePrices(applicationDate, productId, brandId);
    }
} 