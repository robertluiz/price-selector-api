package com.example.priceselectorapi.infrastructure.web.handler;

import com.example.priceselectorapi.application.dto.PriceResponseDTO;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface ErrorHandler<T extends Throwable> {
    
    boolean canHandle(Throwable throwable);
    
    Mono<ResponseEntity<PriceResponseDTO>> handle(T throwable);
    
    int getOrder();
} 