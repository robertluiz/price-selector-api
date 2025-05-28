package com.example.priceselectorapi.infrastructure.web.handler;

import com.example.priceselectorapi.application.dto.PriceResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import jakarta.validation.ConstraintViolationException;

@Component
@Order(1)
@Slf4j
public class ValidationErrorHandler implements ErrorHandler<Exception> {

    @Override
    public boolean canHandle(Throwable throwable) {
        return throwable instanceof ConstraintViolationException ||
               throwable instanceof WebExchangeBindException ||
               throwable instanceof IllegalArgumentException;
    }

    @Override
    public Mono<ResponseEntity<PriceResponseDTO>> handle(Exception throwable) {
        log.warn("Validation error: {}", throwable.getMessage());
        return Mono.just(ResponseEntity.badRequest().build());
    }

    @Override
    public int getOrder() {
        return 1;
    }
} 