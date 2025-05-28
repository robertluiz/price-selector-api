package com.example.priceselectorapi.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class QueryConfig {

    @Value("${app.queries.price-queries-file:queries/price-queries.sql}")
    private String priceQueriesFile;

    @Bean
    public Map<String, String> sqlQueries() {
        Map<String, String> queries = new HashMap<>();
        
        try {
            String priceQueries = loadQueryFromFile(priceQueriesFile);
            queries.put("findApplicablePrices", priceQueries);
            
            log.info("Loaded {} SQL queries from external files", queries.size());
            log.debug("Price queries loaded from: {}", priceQueriesFile);
            
        } catch (IOException e) {
            log.error("Failed to load SQL queries from files", e);
            throw new RuntimeException("Failed to initialize SQL queries", e);
        }
        
        return queries;
    }
    
    private String loadQueryFromFile(String filePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
} 