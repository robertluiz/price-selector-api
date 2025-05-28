package com.example.priceselectorapi.domain.model;

import com.example.priceselectorapi.domain.model.valueobject.DateRange;
import com.example.priceselectorapi.domain.model.valueobject.Money;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("PRICES") 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Price {

    @Id
    private Long id;
    private Integer brandId;
    
    @Transient
    private DateRange validityPeriod;
    
    private Integer priceList;
    private Long productId;
    private Integer priority;
    
    @Transient
    private Money price;
    
    // Database mapping fields
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal priceAmount;
    private String curr;
    
    public boolean isApplicableFor(LocalDateTime applicationDate, Long productId, Integer brandId) {
        return this.productId.equals(productId) 
                && this.brandId.equals(brandId)
                && (validityPeriod != null ? validityPeriod.contains(applicationDate) : 
                    isWithinDateRange(applicationDate));
    }
    
    public boolean hasHigherPriorityThan(Price other) {
        return this.priority.compareTo(other.priority) > 0;
    }
    
    public boolean isActiveAt(LocalDateTime applicationDate) {
        return validityPeriod != null ? validityPeriod.isActive(applicationDate) : 
               isWithinDateRange(applicationDate);
    }
    
    private boolean isWithinDateRange(LocalDateTime applicationDate) {
        return !applicationDate.isBefore(startDate) && !applicationDate.isAfter(endDate);
    }
    
    // Getters for Value Objects (lazy initialization)
    public DateRange getValidityPeriod() {
        if (validityPeriod == null && startDate != null && endDate != null) {
            validityPeriod = DateRange.of(startDate, endDate);
        }
        return validityPeriod;
    }
    
    public Money getPrice() {
        if (price == null && priceAmount != null && curr != null) {
            price = Money.of(priceAmount, curr);
        }
        return price;
    }
    
    // Factory methods for creating Price with Value Objects
    public static Price createWithValueObjects(Long id, Integer brandId, DateRange validityPeriod, 
                                             Integer priceList, Long productId, Integer priority, Money price) {
        return Price.builder()
                .id(id)
                .brandId(brandId)
                .validityPeriod(validityPeriod)
                .priceList(priceList)
                .productId(productId)
                .priority(priority)
                .price(price)
                .startDate(validityPeriod.getStartDate())
                .endDate(validityPeriod.getEndDate())
                .priceAmount(price.getAmount())
                .curr(price.getCurrencyCode())
                .build();
    }
} 