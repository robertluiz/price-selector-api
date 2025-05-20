package com.example.priceselectorapi.infrastructure.web.controller;

import com.example.priceselectorapi.application.dto.PriceResponseDTO;
import com.example.priceselectorapi.application.service.PriceQueryService;
import com.example.priceselectorapi.domain.model.Price;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/prices") 
@RequiredArgsConstructor
@Validated 
public class PriceController {

    private final PriceQueryService priceQueryService;

    @GetMapping("/query")
    public ResponseEntity<PriceResponseDTO> getApplicablePrice(
            @RequestParam("applicationDate") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate,
            @RequestParam("productId") @NotNull @Min(1) Long productId,
            @RequestParam("brandId") @NotNull @Min(1) Integer brandId) {

        Optional<Price> priceOptional = priceQueryService.findApplicablePrice(applicationDate, productId, brandId);

        if (priceOptional.isPresent()) {
            Price price = priceOptional.get();
            PriceResponseDTO responseDTO = PriceResponseDTO.builder()
                    .productId(price.getProductId())
                    .brandId(price.getBrandId())
                    .priceList(price.getPriceList())
                    .startDate(price.getStartDate())
                    .endDate(price.getEndDate())
                    .finalPrice(price.getPriceAmount())
                    .currency(price.getCurr())
                    .build();
            return ResponseEntity.ok(responseDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 