package com.example.priceselectorapi.domain.model.valueobject;

import lombok.Value;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Currency;

@Value
@Builder
public class Money {
    BigDecimal amount;
    Currency currency;
    
    public static Money of(BigDecimal amount, String currencyCode) {
        return Money.builder()
                .amount(amount)
                .currency(Currency.getInstance(currencyCode))
                .build();
    }
    
    public static Money of(BigDecimal amount, Currency currency) {
        return Money.builder()
                .amount(amount)
                .currency(currency)
                .build();
    }
    
    public String getCurrencyCode() {
        return currency.getCurrencyCode();
    }
    
    public boolean isEqualTo(Money other) {
        return this.amount.compareTo(other.amount) == 0 && 
               this.currency.equals(other.currency);
    }
} 