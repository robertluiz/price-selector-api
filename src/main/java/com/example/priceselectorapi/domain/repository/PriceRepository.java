package com.example.priceselectorapi.domain.repository;

import com.example.priceselectorapi.domain.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    /**
     * Finds prices that match the given brandId, productId, and an applicationDate 
     * that falls within the price's startDate and endDate.
     * Results are ordered by priority in descending order to fetch the highest priority first.
     * The query for this method is defined in META-INF/jpa-named-queries.properties
     * 
     * @param applicationDate the date for which the price is applicable.
     * @param productId the ID of the product.
     * @param brandId the ID of the brand.
     * @return a list of Price entities matching the criteria, ordered by priority desc.
     */
    List<Price> findApplicablePrices(
            @Param("applicationDate") LocalDateTime applicationDate,
            @Param("productId") Long productId,
            @Param("brandId") Integer brandId
    );

} 