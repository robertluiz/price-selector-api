package com.example.priceselectorapi.infrastructure.web.controller;

import com.example.priceselectorapi.application.dto.PriceResponseDTO;
import com.example.priceselectorapi.application.service.PriceQueryService;
import com.example.priceselectorapi.domain.model.Price;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/prices") 
@RequiredArgsConstructor
@Validated
@Slf4j
public class PriceController {

    private final PriceQueryService priceQueryService;

    @GetMapping("/query")
    public Mono<ResponseEntity<PriceResponseDTO>> getApplicablePrice(
            @RequestParam("applicationDate") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate,
            @RequestParam("productId") @NotNull @Min(1) Long productId,
            @RequestParam("brandId") @NotNull @Min(1) Integer brandId) {

        log.debug("Querying price for productId: {}, brandId: {}, date: {}", productId, brandId, applicationDate);

        return priceQueryService.findApplicablePrice(applicationDate, productId, brandId)
                .map(this::mapToPriceResponseDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnSuccess(response -> log.debug("Price query completed with status: {}", 
                    response.getStatusCode()));
    }

    private PriceResponseDTO mapToPriceResponseDTO(Price price) {
        return PriceResponseDTO.builder()
                .productId(price.getProductId())
                .brandId(price.getBrandId())
                .priceList(price.getPriceList())
                .startDate(price.getStartDate())
                .endDate(price.getEndDate())
                .finalPrice(price.getPriceAmount())
                .currency(price.getCurr())
                .build();
    }
} 