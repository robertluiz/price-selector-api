package com.example.priceselectorapi.application.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactiveCacheServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private ReactiveCacheService reactiveCacheService;

    @Test
    void get_whenCacheHit_shouldReturnCachedValue() {
        String key = "test-key";
        String cachedValue = "cached-value";
        Cache.ValueWrapper valueWrapper = mock(Cache.ValueWrapper.class);

        when(cacheManager.getCache("prices")).thenReturn(cache);
        when(cache.get(key)).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(cachedValue);

        Supplier<Mono<Object>> supplier = () -> Mono.just("new-value");

        StepVerifier.create(reactiveCacheService.get(key, supplier))
                .expectNext(cachedValue)
                .verifyComplete();

        verify(cache).get(key);
        verify(cache, never()).put(any(), any());
    }

    @Test
    void get_whenCacheMiss_shouldExecuteSupplierAndCache() {
        String key = "test-key";
        String newValue = "new-value";

        when(cacheManager.getCache("prices")).thenReturn(cache);
        when(cache.get(key)).thenReturn(null);

        Supplier<Mono<Object>> supplier = () -> Mono.just(newValue);

        StepVerifier.create(reactiveCacheService.get(key, supplier))
                .expectNext(newValue)
                .verifyComplete();

        verify(cache).get(key);
        verify(cache).put(key, newValue);
    }

    @Test
    void get_whenCacheNotFound_shouldExecuteSupplierDirectly() {
        String key = "test-key";
        String newValue = "new-value";

        when(cacheManager.getCache("prices")).thenReturn(null);

        Supplier<Mono<Object>> supplier = () -> Mono.just(newValue);

        StepVerifier.create(reactiveCacheService.get(key, supplier))
                .expectNext(newValue)
                .verifyComplete();

        verify(cacheManager).getCache("prices");
        verifyNoMoreInteractions(cache);
    }

    @Test
    void evict_shouldEvictFromCache() {
        String key = "test-key";

        when(cacheManager.getCache("prices")).thenReturn(cache);

        reactiveCacheService.evict(key);

        verify(cache).evict(key);
    }

    @Test
    void clear_shouldClearCache() {
        when(cacheManager.getCache("prices")).thenReturn(cache);

        reactiveCacheService.clear();

        verify(cache).clear();
    }
} 