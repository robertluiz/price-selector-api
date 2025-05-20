package com.example.priceselectorapi.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceResponseDTO {
    private Long productId;
    private Integer brandId;
    private Integer priceList;
    private LocalDateTime startDate; 
    private LocalDateTime endDate;   
    private BigDecimal finalPrice; 
    private String currency;
} 