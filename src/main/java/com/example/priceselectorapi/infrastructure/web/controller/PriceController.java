package com.example.priceselectorapi.infrastructure.web.controller;

import com.example.priceselectorapi.application.dto.PriceResponseDTO;
import com.example.priceselectorapi.application.mapper.PriceMapper;
import com.example.priceselectorapi.application.service.PriceQueryService;
import com.example.priceselectorapi.infrastructure.web.handler.ErrorHandler;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/prices") 
@RequiredArgsConstructor
@Validated
@Slf4j
public class PriceController {

    private final PriceQueryService priceQueryService;
    private final PriceMapper priceMapper;
    private final List<ErrorHandler<? extends Throwable>> errorHandlers;

    @GetMapping("/query")
    public Mono<ResponseEntity<PriceResponseDTO>> getApplicablePrice(
            @RequestParam("applicationDate") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate,
            @RequestParam("productId") @NotNull @Min(1) Long productId,
            @RequestParam("brandId") @NotNull @Min(1) Integer brandId) {

        log.debug("Querying price for productId: {}, brandId: {}, date: {}", productId, brandId, applicationDate);

        return priceQueryService.findApplicablePrice(applicationDate, productId, brandId)
                .map(priceMapper::toResponseDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(this::handleError)
                .doOnSuccess(response -> log.debug("Price query completed with status: {}", 
                    response.getStatusCode()));
    }
    
    @SuppressWarnings("unchecked")
    private Mono<ResponseEntity<PriceResponseDTO>> handleError(Throwable throwable) {
        log.debug("Handling error with chain of responsibility: {}", throwable.getClass().getSimpleName());
        
        return errorHandlers.stream()
                .sorted((h1, h2) -> Integer.compare(h1.getOrder(), h2.getOrder()))
                .filter(handler -> handler.canHandle(throwable))
                .findFirst()
                .map(handler -> ((ErrorHandler<Throwable>) handler).handle(throwable))
                .orElseGet(() -> {
                    log.error("No handler found for error: {}", throwable.getMessage(), throwable);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
} 