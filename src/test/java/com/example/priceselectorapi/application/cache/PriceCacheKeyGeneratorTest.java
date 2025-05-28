package com.example.priceselectorapi.application.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PriceCacheKeyGeneratorTest {

    @InjectMocks
    private PriceCacheKeyGenerator cacheKeyGenerator;

    @Test
    void generateKey_shouldCreateCorrectFormat() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        Long productId = 35455L;
        Integer brandId = 1;

        String result = cacheKeyGenerator.generateKey(applicationDate, productId, brandId);

        assertThat(result).isEqualTo("2020-06-14T10:00_35455_1");
    }

    @Test
    void generateKey_shouldHandleDifferentValues() {
        LocalDateTime applicationDate = LocalDateTime.of(2021, 12, 25, 23, 59, 59);
        Long productId = 99999L;
        Integer brandId = 999;

        String result = cacheKeyGenerator.generateKey(applicationDate, productId, brandId);

        assertThat(result).isEqualTo("2021-12-25T23:59:59_99999_999");
    }

    @Test
    void generateKey_shouldBeConsistent() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        Long productId = 35455L;
        Integer brandId = 1;

        String result1 = cacheKeyGenerator.generateKey(applicationDate, productId, brandId);
        String result2 = cacheKeyGenerator.generateKey(applicationDate, productId, brandId);

        assertThat(result1).isEqualTo(result2);
    }
} 