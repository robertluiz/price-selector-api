package com.example.priceselectorapi.infrastructure.repository.mapper;

import com.example.priceselectorapi.domain.model.Price;
import com.example.priceselectorapi.domain.model.valueobject.DateRange;
import com.example.priceselectorapi.domain.model.valueobject.Money;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Slf4j
public class PriceRowMapper {

    public Price mapRowToPrice(Row row, RowMetadata metadata) {
        log.debug("Mapping database row to Price entity");
        
        try {
            Long id = row.get("id", Long.class);
            Integer brandId = row.get("brand_id", Integer.class);
            LocalDateTime startDate = row.get("start_date", LocalDateTime.class);
            LocalDateTime endDate = row.get("end_date", LocalDateTime.class);
            Integer priceList = row.get("price_list", Integer.class);
            Long productId = row.get("product_id", Long.class);
            Integer priority = row.get("priority", Integer.class);
            BigDecimal priceAmount = row.get("price_amount", BigDecimal.class);
            String currency = row.get("curr", String.class);

            DateRange validityPeriod = DateRange.of(startDate, endDate);
            Money price = Money.of(priceAmount, currency);

            return Price.builder()
                    .id(id)
                    .brandId(brandId)
                    .validityPeriod(validityPeriod)
                    .priceList(priceList)
                    .productId(productId)
                    .priority(priority)
                    .price(price)
                    .startDate(startDate)
                    .endDate(endDate)
                    .priceAmount(priceAmount)
                    .curr(currency)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error mapping database row to Price entity", e);
            throw new RuntimeException("Failed to map database row to Price entity", e);
        }
    }
} 