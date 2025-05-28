package com.example.priceselectorapi.application.mapper;

import com.example.priceselectorapi.application.dto.PriceResponseDTO;
import com.example.priceselectorapi.domain.model.Price;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PriceMapperTest {

    @InjectMocks
    private PriceMapper priceMapper;

    @Test
    void toResponseDTO_shouldMapAllFieldsCorrectly() {
        LocalDateTime startDate = LocalDateTime.of(2020, 6, 14, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2020, 12, 31, 23, 59, 59);
        
        Price price = Price.builder()
                .id(1L)
                .productId(35455L)
                .brandId(1)
                .priceList(1)
                .startDate(startDate)
                .endDate(endDate)
                .priceAmount(new BigDecimal("35.50"))
                .curr("EUR")
                .priority(0)
                .build();

        PriceResponseDTO result = priceMapper.toResponseDTO(price);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(35455L);
        assertThat(result.getBrandId()).isEqualTo(1);
        assertThat(result.getPriceList()).isEqualTo(1);
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
        assertThat(result.getFinalPrice()).isEqualTo(new BigDecimal("35.50"));
        assertThat(result.getCurrency()).isEqualTo("EUR");
    }
} 