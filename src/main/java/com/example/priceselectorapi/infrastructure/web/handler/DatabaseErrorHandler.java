package com.example.priceselectorapi.infrastructure.web.handler;

import com.example.priceselectorapi.application.dto.PriceResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Order(2)
@Slf4j
public class DatabaseErrorHandler implements ErrorHandler<DataAccessException> {

    @Override
    public boolean canHandle(Throwable throwable) {
        return throwable instanceof DataAccessException;
    }

    @Override
    public Mono<ResponseEntity<PriceResponseDTO>> handle(DataAccessException throwable) {
        log.error("Database error: {}", throwable.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    @Override
    public int getOrder() {
        return 2;
    }
} 