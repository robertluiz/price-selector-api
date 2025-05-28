package com.example.priceselectorapi.domain.model.valueobject;

import lombok.Value;
import lombok.Builder;

import java.time.LocalDateTime;

@Value
@Builder
public class DateRange {
    LocalDateTime startDate;
    LocalDateTime endDate;
    
    public static DateRange of(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        return DateRange.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
    
    public boolean contains(LocalDateTime date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    public boolean isActive(LocalDateTime applicationDate) {
        return contains(applicationDate);
    }
    
    public boolean overlaps(DateRange other) {
        return !this.endDate.isBefore(other.startDate) && 
               !other.endDate.isBefore(this.startDate);
    }
} 