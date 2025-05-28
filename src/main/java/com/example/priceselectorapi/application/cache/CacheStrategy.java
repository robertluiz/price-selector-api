package com.example.priceselectorapi.application.cache;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public interface CacheStrategy<T> {
    
    Mono<T> get(String key, Supplier<Mono<T>> valueSupplier);
    
    void evict(String key);
    
    void clear();
} 