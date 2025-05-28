package com.example.priceselectorapi.domain.model.factory;

import com.example.priceselectorapi.domain.model.Price;
import com.example.priceselectorapi.domain.model.valueobject.DateRange;
import com.example.priceselectorapi.domain.model.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PriceFactory Tests")
class PriceFactoryTest {

    private final LocalDateTime startDate = LocalDateTime.of(2020, 6, 14, 10, 0);
    private final LocalDateTime endDate = LocalDateTime.of(2020, 6, 15, 18, 0);
    private final BigDecimal amount = new BigDecimal("35.50");
    private final String currencyCode = "EUR";

    @Nested
    @DisplayName("Create Price Tests")
    class CreatePriceTests {

        @Test
        @DisplayName("Should create Price with valid parameters")
        void shouldCreatePriceWithValidParameters() {
            Price price = PriceFactory.createPrice(
                    1L, 1, 1, 35455L, 1,
                    startDate, endDate, amount, currencyCode
            );

            assertThat(price.getId()).isEqualTo(1L);
            assertThat(price.getBrandId()).isEqualTo(1);
            assertThat(price.getPriceList()).isEqualTo(1);
            assertThat(price.getProductId()).isEqualTo(35455L);
            assertThat(price.getPriority()).isEqualTo(1);
            assertThat(price.getStartDate()).isEqualTo(startDate);
            assertThat(price.getEndDate()).isEqualTo(endDate);
            assertThat(price.getPriceAmount()).isEqualTo(amount);
            assertThat(price.getCurr()).isEqualTo(currencyCode);
            
            assertThat(price.getValidityPeriod()).isNotNull();
            assertThat(price.getPrice()).isNotNull();
        }

        @Test
        @DisplayName("Should create Price with zero priority")
        void shouldCreatePriceWithZeroPriority() {
            Price price = PriceFactory.createPrice(
                    1L, 1, 1, 35455L, 0,
                    startDate, endDate, amount, currencyCode
            );

            assertThat(price.getPriority()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create Price with zero amount")
        void shouldCreatePriceWithZeroAmount() {
            Price price = PriceFactory.createPrice(
                    1L, 1, 1, 35455L, 1,
                    startDate, endDate, BigDecimal.ZERO, currencyCode
            );

            assertThat(price.getPriceAmount()).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Create Price with Value Objects Tests")
    class CreatePriceWithValueObjectsTests {

        @Test
        @DisplayName("Should create Price with Value Objects")
        void shouldCreatePriceWithValueObjects() {
            DateRange dateRange = DateRange.of(startDate, endDate);
            Money money = Money.of(amount, currencyCode);

            Price price = PriceFactory.createPriceWithValueObjects(
                    1L, 1, 1, 35455L, 1, dateRange, money
            );

            assertThat(price.getId()).isEqualTo(1L);
            assertThat(price.getBrandId()).isEqualTo(1);
            assertThat(price.getPriceList()).isEqualTo(1);
            assertThat(price.getProductId()).isEqualTo(35455L);
            assertThat(price.getPriority()).isEqualTo(1);
            assertThat(price.getValidityPeriod()).isEqualTo(dateRange);
            assertThat(price.getPrice()).isEqualTo(money);
            
            assertThat(price.getStartDate()).isEqualTo(startDate);
            assertThat(price.getEndDate()).isEqualTo(endDate);
            assertThat(price.getPriceAmount()).isEqualTo(amount);
            assertThat(price.getCurr()).isEqualTo(currencyCode);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception for null brand ID")
        void shouldThrowExceptionForNullBrandId() {
            assertThatThrownBy(() -> PriceFactory.createPrice(
                    1L, null, 1, 35455L, 1,
                    startDate, endDate, amount, currencyCode
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("Brand ID must be positive");
        }

        @Test
        @DisplayName("Should throw exception for negative brand ID")
        void shouldThrowExceptionForNegativeBrandId() {
            assertThatThrownBy(() -> PriceFactory.createPrice(
                    1L, -1, 1, 35455L, 1,
                    startDate, endDate, amount, currencyCode
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("Brand ID must be positive");
        }

        @Test
        @DisplayName("Should throw exception for null product ID")
        void shouldThrowExceptionForNullProductId() {
            assertThatThrownBy(() -> PriceFactory.createPrice(
                    1L, 1, 1, null, 1,
                    startDate, endDate, amount, currencyCode
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("Product ID must be positive");
        }

        @Test
        @DisplayName("Should throw exception for null priority")
        void shouldThrowExceptionForNullPriority() {
            assertThatThrownBy(() -> PriceFactory.createPrice(
                    1L, 1, 1, 35455L, null,
                    startDate, endDate, amount, currencyCode
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("Priority cannot be negative");
        }

        @Test
        @DisplayName("Should throw exception for negative priority")
        void shouldThrowExceptionForNegativePriority() {
            assertThatThrownBy(() -> PriceFactory.createPrice(
                    1L, 1, 1, 35455L, -1,
                    startDate, endDate, amount, currencyCode
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("Priority cannot be negative");
        }

        @Test
        @DisplayName("Should throw exception for null start date")
        void shouldThrowExceptionForNullStartDate() {
            assertThatThrownBy(() -> PriceFactory.createPrice(
                    1L, 1, 1, 35455L, 1,
                    null, endDate, amount, currencyCode
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("Start date cannot be null");
        }

        @Test
        @DisplayName("Should throw exception for null end date")
        void shouldThrowExceptionForNullEndDate() {
            assertThatThrownBy(() -> PriceFactory.createPrice(
                    1L, 1, 1, 35455L, 1,
                    startDate, null, amount, currencyCode
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("End date cannot be null");
        }

        @Test
        @DisplayName("Should throw exception for null amount")
        void shouldThrowExceptionForNullAmount() {
            assertThatThrownBy(() -> PriceFactory.createPrice(
                    1L, 1, 1, 35455L, 1,
                    startDate, endDate, null, currencyCode
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("Price amount cannot be null or negative");
        }

        @Test
        @DisplayName("Should throw exception for negative amount")
        void shouldThrowExceptionForNegativeAmount() {
            assertThatThrownBy(() -> PriceFactory.createPrice(
                    1L, 1, 1, 35455L, 1,
                    startDate, endDate, new BigDecimal("-10"), currencyCode
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("Price amount cannot be null or negative");
        }

        @Test
        @DisplayName("Should throw exception for null currency code")
        void shouldThrowExceptionForNullCurrencyCode() {
            assertThatThrownBy(() -> PriceFactory.createPrice(
                    1L, 1, 1, 35455L, 1,
                    startDate, endDate, amount, null
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("Currency code cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw exception for empty currency code")
        void shouldThrowExceptionForEmptyCurrencyCode() {
            assertThatThrownBy(() -> PriceFactory.createPrice(
                    1L, 1, 1, 35455L, 1,
                    startDate, endDate, amount, ""
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("Currency code cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw exception for null DateRange in Value Objects creation")
        void shouldThrowExceptionForNullDateRange() {
            Money money = Money.of(amount, currencyCode);

            assertThatThrownBy(() -> PriceFactory.createPriceWithValueObjects(
                    1L, 1, 1, 35455L, 1, null, money
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("DateRange cannot be null");
        }

        @Test
        @DisplayName("Should throw exception for null Money in Value Objects creation")
        void shouldThrowExceptionForNullMoney() {
            DateRange dateRange = DateRange.of(startDate, endDate);

            assertThatThrownBy(() -> PriceFactory.createPriceWithValueObjects(
                    1L, 1, 1, 35455L, 1, dateRange, null
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("Money cannot be null");
        }
    }
} 