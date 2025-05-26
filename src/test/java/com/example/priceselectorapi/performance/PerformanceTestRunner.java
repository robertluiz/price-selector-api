package com.example.priceselectorapi.performance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class PerformanceTestRunner {

    private static final String BASE_URL = "http://localhost:8081";
    private static final int CONCURRENT_USERS = 50;
    private static final int REQUESTS_PER_USER = 20;
    private static final Duration TEST_DURATION = Duration.ofMinutes(1);
    
    private final WebClient webClient;
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);

    public PerformanceTestRunner() {
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    public static void main(String[] args) {
        PerformanceTestRunner runner = new PerformanceTestRunner();
        runner.runPerformanceTest();
    }

    public void runPerformanceTest() {
        System.out.println("=== JAVA PERFORMANCE TEST STARTING ===");
        System.out.println("Configuration:");
        System.out.println("- Concurrent Users: " + CONCURRENT_USERS);
        System.out.println("- Requests per User: " + REQUESTS_PER_USER);
        System.out.println("- Total Requests: " + (CONCURRENT_USERS * REQUESTS_PER_USER));
        System.out.println("- Target URL: " + BASE_URL);
        System.out.println("==========================================");
        
        long startTime = System.currentTimeMillis();
        
        try {
            Flux.range(0, CONCURRENT_USERS)
                    .flatMap(this::simulateUser, 25)
                    .blockLast(TEST_DURATION);
        } catch (Exception e) {
            System.err.println("Performance test failed: " + e.getMessage());
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        printResults(totalTime);
    }

    private Flux<String> simulateUser(int userId) {
        return Flux.range(0, REQUESTS_PER_USER)
                .flatMap(requestId -> makeRequest(userId, requestId))
                .onErrorContinue((throwable, o) -> {
                    errorCount.incrementAndGet();
                    if (errorCount.get() % 100 == 0) {
                        System.out.println("⚠️  " + errorCount.get() + " errors so far...");
                    }
                });
    }

    private Mono<String> makeRequest(int userId, int requestId) {
        long requestStart = System.currentTimeMillis();
        
        String applicationDate = getTestDate(requestId);
        Long productId = getTestProductId(requestId);
        Integer brandId = getTestBrandId(requestId);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/prices/query")
                        .queryParam("applicationDate", applicationDate)
                        .queryParam("productId", productId)
                        .queryParam("brandId", brandId)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> {
                    long responseTime = System.currentTimeMillis() - requestStart;
                    totalResponseTime.addAndGet(responseTime);
                    int currentSuccess = successCount.incrementAndGet();
                    
                    if (currentSuccess % 100 == 0) {
                        System.out.println("✅ " + currentSuccess + " successful requests completed");
                    }
                })
                .doOnError(error -> {
                    errorCount.incrementAndGet();
                })
                .onErrorReturn("ERROR");
    }

    private String getTestDate(int requestId) {
        String[] testDates = {
            "2020-06-14T10:00:00",
            "2020-06-14T16:00:00", 
            "2020-06-14T21:00:00",
            "2020-06-15T10:00:00",
            "2020-06-16T21:00:00"
        };
        return testDates[requestId % testDates.length];
    }

    private Long getTestProductId(int requestId) {
        return 35455L; // Using the main test product ID
    }

    private Integer getTestBrandId(int requestId) {
        return 1; // Using the main test brand ID (ZARA)
    }

    private void printResults(long totalTime) {
        int totalRequests = successCount.get() + errorCount.get();
        double requestsPerSecond = (double) totalRequests / (totalTime / 1000.0);
        double successfulRps = (double) successCount.get() / (totalTime / 1000.0);
        double averageResponseTime = totalResponseTime.get() / (double) Math.max(successCount.get(), 1);
        double successRate = (double) successCount.get() / totalRequests * 100;
        
        System.out.println("\n=== JAVA PERFORMANCE TEST RESULTS ===");
        System.out.println("Total time: " + totalTime + "ms (" + String.format("%.1f", totalTime/1000.0) + "s)");
        System.out.println("Total requests: " + totalRequests);
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + errorCount.get());
        System.out.println("Success rate: " + String.format("%.2f", successRate) + "%");
        System.out.println("Total RPS: " + String.format("%.2f", requestsPerSecond));
        System.out.println("Successful RPS: " + String.format("%.2f", successfulRps));
        System.out.println("Average response time: " + String.format("%.2f", averageResponseTime) + "ms");
        System.out.println("======================================");
        
        if (successfulRps >= 100) {
            System.out.println("🎉 EXCELLENT PERFORMANCE!");
            System.out.println("✅ Target exceeded: " + String.format("%.0f", successfulRps) + " RPS > 100 RPS target");
            
            if (successfulRps >= 500) {
                System.out.println("🚀 EXCEPTIONAL: " + String.format("%.0f", successfulRps/100) + "x better than target!");
            }
            
            if (averageResponseTime < 100) {
                System.out.println("⚡ ULTRA-FAST: Average response time under 100ms");
            }
            
            System.exit(0);
        } else {
            System.out.println("⚠️  Performance below target of 100 RPS");
            System.out.println("Achieved: " + String.format("%.0f", successfulRps) + " RPS");
            System.exit(1);
        }
    }
} 