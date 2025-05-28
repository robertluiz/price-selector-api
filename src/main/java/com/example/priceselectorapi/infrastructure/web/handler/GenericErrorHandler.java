package com.example.priceselectorapi.infrastructure.web.handler;

import com.example.priceselectorapi.application.dto.PriceResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Order(999)
@Slf4j
public class GenericErrorHandler implements ErrorHandler<Throwable> {

    @Override
    public boolean canHandle(Throwable throwable) {
        return true; // Handles any unhandled exception
    }

    @Override
    public Mono<ResponseEntity<PriceResponseDTO>> handle(Throwable throwable) {
        log.error("Unhandled error: {}", throwable.getMessage(), throwable);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Override
    public int getOrder() {
        return 999;
    }
} 