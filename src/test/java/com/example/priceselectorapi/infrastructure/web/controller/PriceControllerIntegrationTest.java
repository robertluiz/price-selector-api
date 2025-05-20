package com.example.priceselectorapi.infrastructure.web.controller;

import com.example.priceselectorapi.application.dto.PriceResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
class PriceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private void performTest(
            String applicationDateStr,
            Long productId,
            Integer brandId,
            BigDecimal expectedPrice,
            Integer expectedPriceList,
            String expectedStartDateStr,
            String expectedEndDateStr) throws Exception {

        MvcResult result = mockMvc.perform(get("/api/v1/prices/query")
                        .param("applicationDate", applicationDateStr)
                        .param("productId", String.valueOf(productId))
                        .param("brandId", String.valueOf(brandId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId", is(productId.intValue())))
                .andExpect(jsonPath("$.brandId", is(brandId)))
                .andExpect(jsonPath("$.priceList", is(expectedPriceList)))
                .andExpect(jsonPath("$.finalPrice", is(expectedPrice.doubleValue())))
                .andExpect(jsonPath("$.currency", is("EUR"))) // Assuming currency is always EUR for these tests
                .andReturn();

        // Verify dates separately due to potential formatting issues with jsonPath for LocalDateTime
        String responseString = result.getResponse().getContentAsString();
        PriceResponseDTO responseDTO = objectMapper.readValue(responseString, PriceResponseDTO.class);

        assertEquals(LocalDateTime.parse(expectedStartDateStr, formatter), responseDTO.getStartDate());
        assertEquals(LocalDateTime.parse(expectedEndDateStr, formatter), responseDTO.getEndDate());
    }

    // Test 1: petición a las 10:00 del día 14 del producto 35455 para la brand 1 (ZARA)
    // Expected: Price List 1, Price 35.50
    @Test
    void testScenario1_1000_Day14() throws Exception {
        performTest(
                "2020-06-14T10:00:00",
                35455L,
                1,
                new BigDecimal("35.50"),
                1,
                "2020-06-14T00:00:00",
                "2020-12-31T23:59:59"
        );
    }

    // Test 2: petición a las 16:00 del día 14 del producto 35455 para la brand 1 (ZARA)
    // Expected: Price List 2, Price 25.45 (Priority 1 for this time range)
    @Test
    void testScenario2_1600_Day14() throws Exception {
        performTest(
                "2020-06-14T16:00:00",
                35455L,
                1,
                new BigDecimal("25.45"),
                2,
                "2020-06-14T15:00:00",
                "2020-06-14T18:30:00"
        );
    }

    // Test 3: petición a las 21:00 del día 14 del producto 35455 para la brand 1 (ZARA)
    // Expected: Price List 1, Price 35.50 (Price list 2 is no longer active)
    @Test
    void testScenario3_2100_Day14() throws Exception {
        performTest(
                "2020-06-14T21:00:00",
                35455L,
                1,
                new BigDecimal("35.50"),
                1,
                "2020-06-14T00:00:00",
                "2020-12-31T23:59:59"
        );
    }

    // Test 4: petición a las 10:00 del día 15 del producto 35455 para la brand 1 (ZARA)
    // Expected: Price List 3, Price 30.50 (Priority 1 for this time range)
    @Test
    void testScenario4_1000_Day15() throws Exception {
        performTest(
                "2020-06-15T10:00:00",
                35455L,
                1,
                new BigDecimal("30.50"),
                3,
                "2020-06-15T00:00:00",
                "2020-06-15T11:00:00"
        );
    }

    // Test 5: petición a las 21:00 del día 16 del producto 35455 para la brand 1 (ZARA)
    // Expected: Price List 4, Price 38.95 (Priority 1 for this time range, after price list 3 ended)
    @Test
    void testScenario5_2100_Day16() throws Exception {
        performTest(
                "2020-06-16T21:00:00",
                35455L,
                1,
                new BigDecimal("38.95"),
                4,
                "2020-06-15T16:00:00",
                "2020-12-31T23:59:59"
        );
    }

    // Helper to assert LocalDateTime equality as jsonPath has trouble with direct comparison
    private void assertEquals(LocalDateTime expected, LocalDateTime actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
} 