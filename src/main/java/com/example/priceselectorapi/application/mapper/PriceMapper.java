package com.example.priceselectorapi.application.mapper;

import com.example.priceselectorapi.application.dto.PriceResponseDTO;
import com.example.priceselectorapi.domain.model.Price;
import org.springframework.stereotype.Component;

@Component
public class PriceMapper {

    public PriceResponseDTO toResponseDTO(Price price) {
        return PriceResponseDTO.builder()
                .productId(price.getProductId())
                .brandId(price.getBrandId())
                .priceList(price.getPriceList())
                .startDate(price.getStartDate())
                .endDate(price.getEndDate())
                .finalPrice(price.getPriceAmount())
                .currency(price.getCurr())
                .build();
    }
} 