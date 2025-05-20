package com.example.priceselectorapi.application.service;

import com.example.priceselectorapi.domain.model.Price;
import com.example.priceselectorapi.domain.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PriceQueryService {

    private static final int FIRST = 0;
    private final PriceRepository priceRepository;

    /**
     * Finds the applicable price for a given product, brand, and application date.
     * If multiple prices are valid for the given date, the one with the highest priority is returned.
     *
     * @param applicationDate The date and time for which the price is requested.
     * @param productId The ID of the product.
     * @param brandId The ID of the brand.
     * @return An Optional containing the applicable Price if found, otherwise an empty Optional.
     */
    public Optional<Price> findApplicablePrice(
            LocalDateTime applicationDate,
            Long productId,
            Integer brandId) {

        List<Price> applicablePrices = priceRepository.findApplicablePrices(applicationDate, productId, brandId);

        if (!applicablePrices.isEmpty()) {
            return Optional.of(applicablePrices.get(FIRST));
        }

        return Optional.empty();
    }
} 