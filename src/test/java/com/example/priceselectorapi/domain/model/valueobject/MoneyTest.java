package com.example.priceselectorapi.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create Money with BigDecimal amount and currency code")
        void shouldCreateMoneyWithAmountAndCurrencyCode() {
            BigDecimal amount = new BigDecimal("35.50");
            String currencyCode = "EUR";

            Money money = Money.of(amount, currencyCode);

            assertThat(money.getAmount()).isEqualTo(amount);
            assertThat(money.getCurrency()).isEqualTo(Currency.getInstance(currencyCode));
            assertThat(money.getCurrencyCode()).isEqualTo(currencyCode);
        }

        @Test
        @DisplayName("Should create Money with BigDecimal amount and Currency object")
        void shouldCreateMoneyWithAmountAndCurrency() {
            BigDecimal amount = new BigDecimal("25.45");
            Currency currency = Currency.getInstance("USD");

            Money money = Money.of(amount, currency);

            assertThat(money.getAmount()).isEqualTo(amount);
            assertThat(money.getCurrency()).isEqualTo(currency);
            assertThat(money.getCurrencyCode()).isEqualTo("USD");
        }

        @Test
        @DisplayName("Should handle zero amount")
        void shouldHandleZeroAmount() {
            BigDecimal amount = BigDecimal.ZERO;
            String currencyCode = "EUR";

            Money money = Money.of(amount, currencyCode);

            assertThat(money.getAmount()).isEqualTo(BigDecimal.ZERO);
            assertThat(money.getCurrencyCode()).isEqualTo(currencyCode);
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should return true for equal Money objects")
        void shouldReturnTrueForEqualMoney() {
            Money money1 = Money.of(new BigDecimal("35.50"), "EUR");
            Money money2 = Money.of(new BigDecimal("35.50"), "EUR");

            assertThat(money1.isEqualTo(money2)).isTrue();
        }

        @Test
        @DisplayName("Should return false for different amounts")
        void shouldReturnFalseForDifferentAmounts() {
            Money money1 = Money.of(new BigDecimal("35.50"), "EUR");
            Money money2 = Money.of(new BigDecimal("25.45"), "EUR");

            assertThat(money1.isEqualTo(money2)).isFalse();
        }

        @Test
        @DisplayName("Should return false for different currencies")
        void shouldReturnFalseForDifferentCurrencies() {
            Money money1 = Money.of(new BigDecimal("35.50"), "EUR");
            Money money2 = Money.of(new BigDecimal("35.50"), "USD");

            assertThat(money1.isEqualTo(money2)).isFalse();
        }

        @Test
        @DisplayName("Should handle BigDecimal scale differences correctly")
        void shouldHandleBigDecimalScaleDifferences() {
            Money money1 = Money.of(new BigDecimal("35.50"), "EUR");
            Money money2 = Money.of(new BigDecimal("35.5"), "EUR");

            assertThat(money1.isEqualTo(money2)).isTrue();
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create Money using builder pattern")
        void shouldCreateMoneyUsingBuilder() {
            BigDecimal amount = new BigDecimal("38.95");
            Currency currency = Currency.getInstance("EUR");

            Money money = Money.builder()
                    .amount(amount)
                    .currency(currency)
                    .build();

            assertThat(money.getAmount()).isEqualTo(amount);
            assertThat(money.getCurrency()).isEqualTo(currency);
        }
    }
} 