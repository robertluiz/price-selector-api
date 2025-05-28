package com.example.priceselectorapi.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DateRange Value Object Tests")
class DateRangeTest {

    private final LocalDateTime baseDate = LocalDateTime.of(2020, 6, 14, 10, 0);

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create DateRange with valid start and end dates")
        void shouldCreateDateRangeWithValidDates() {
            LocalDateTime startDate = baseDate;
            LocalDateTime endDate = baseDate.plusDays(1);

            DateRange dateRange = DateRange.of(startDate, endDate);

            assertThat(dateRange.getStartDate()).isEqualTo(startDate);
            assertThat(dateRange.getEndDate()).isEqualTo(endDate);
        }

        @Test
        @DisplayName("Should create DateRange with same start and end dates")
        void shouldCreateDateRangeWithSameDates() {
            LocalDateTime date = baseDate;

            DateRange dateRange = DateRange.of(date, date);

            assertThat(dateRange.getStartDate()).isEqualTo(date);
            assertThat(dateRange.getEndDate()).isEqualTo(date);
        }

        @Test
        @DisplayName("Should throw exception when start date is after end date")
        void shouldThrowExceptionWhenStartDateAfterEndDate() {
            LocalDateTime startDate = baseDate.plusDays(1);
            LocalDateTime endDate = baseDate;

            assertThatThrownBy(() -> DateRange.of(startDate, endDate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Start date cannot be after end date");
        }
    }

    @Nested
    @DisplayName("Contains Tests")
    class ContainsTests {

        @Test
        @DisplayName("Should return true when date is within range")
        void shouldReturnTrueWhenDateWithinRange() {
            DateRange dateRange = DateRange.of(baseDate, baseDate.plusDays(2));
            LocalDateTime testDate = baseDate.plusDays(1);

            assertThat(dateRange.contains(testDate)).isTrue();
        }

        @Test
        @DisplayName("Should return true when date equals start date")
        void shouldReturnTrueWhenDateEqualsStartDate() {
            DateRange dateRange = DateRange.of(baseDate, baseDate.plusDays(1));

            assertThat(dateRange.contains(baseDate)).isTrue();
        }

        @Test
        @DisplayName("Should return true when date equals end date")
        void shouldReturnTrueWhenDateEqualsEndDate() {
            LocalDateTime endDate = baseDate.plusDays(1);
            DateRange dateRange = DateRange.of(baseDate, endDate);

            assertThat(dateRange.contains(endDate)).isTrue();
        }

        @Test
        @DisplayName("Should return false when date is before range")
        void shouldReturnFalseWhenDateBeforeRange() {
            DateRange dateRange = DateRange.of(baseDate, baseDate.plusDays(1));
            LocalDateTime testDate = baseDate.minusDays(1);

            assertThat(dateRange.contains(testDate)).isFalse();
        }

        @Test
        @DisplayName("Should return false when date is after range")
        void shouldReturnFalseWhenDateAfterRange() {
            DateRange dateRange = DateRange.of(baseDate, baseDate.plusDays(1));
            LocalDateTime testDate = baseDate.plusDays(2);

            assertThat(dateRange.contains(testDate)).isFalse();
        }
    }

    @Nested
    @DisplayName("Active Tests")
    class ActiveTests {

        @Test
        @DisplayName("Should return true when date range is active at application date")
        void shouldReturnTrueWhenActiveAtDate() {
            DateRange dateRange = DateRange.of(baseDate, baseDate.plusDays(1));

            assertThat(dateRange.isActive(baseDate.plusHours(12))).isTrue();
        }

        @Test
        @DisplayName("Should return false when date range is not active at application date")
        void shouldReturnFalseWhenNotActiveAtDate() {
            DateRange dateRange = DateRange.of(baseDate, baseDate.plusDays(1));

            assertThat(dateRange.isActive(baseDate.plusDays(2))).isFalse();
        }
    }

    @Nested
    @DisplayName("Overlaps Tests")
    class OverlapsTests {

        @Test
        @DisplayName("Should return true for overlapping ranges")
        void shouldReturnTrueForOverlappingRanges() {
            DateRange range1 = DateRange.of(baseDate, baseDate.plusDays(2));
            DateRange range2 = DateRange.of(baseDate.plusDays(1), baseDate.plusDays(3));

            assertThat(range1.overlaps(range2)).isTrue();
            assertThat(range2.overlaps(range1)).isTrue();
        }

        @Test
        @DisplayName("Should return true for adjacent ranges")
        void shouldReturnTrueForAdjacentRanges() {
            DateRange range1 = DateRange.of(baseDate, baseDate.plusDays(1));
            DateRange range2 = DateRange.of(baseDate.plusDays(1), baseDate.plusDays(2));

            assertThat(range1.overlaps(range2)).isTrue();
            assertThat(range2.overlaps(range1)).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-overlapping ranges")
        void shouldReturnFalseForNonOverlappingRanges() {
            DateRange range1 = DateRange.of(baseDate, baseDate.plusDays(1));
            DateRange range2 = DateRange.of(baseDate.plusDays(2), baseDate.plusDays(3));

            assertThat(range1.overlaps(range2)).isFalse();
            assertThat(range2.overlaps(range1)).isFalse();
        }

        @Test
        @DisplayName("Should return true for contained ranges")
        void shouldReturnTrueForContainedRanges() {
            DateRange outerRange = DateRange.of(baseDate, baseDate.plusDays(5));
            DateRange innerRange = DateRange.of(baseDate.plusDays(1), baseDate.plusDays(2));

            assertThat(outerRange.overlaps(innerRange)).isTrue();
            assertThat(innerRange.overlaps(outerRange)).isTrue();
        }
    }
} 