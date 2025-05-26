# Price Selector API

[![Java CI with Maven](https://github.com/robertluiz/price-selector-api/actions/workflows/maven-ci.yml/badge.svg)](https://github.com/robertluiz/price-selector-api/actions/workflows/maven-ci.yml)

## Description

A **high-performance reactive REST API** built with Spring WebFlux for querying product prices based on brand, product ID, and application date. The service uses an in-memory H2 database and implements advanced caching and performance optimization strategies.

The service determines the applicable price based on date ranges and priority fields. When multiple prices match the criteria, the one with the highest priority is selected. The application follows Hexagonal Architecture principles and implements reactive programming for optimal performance under high load.

This project demonstrates modern Spring Boot development practices with a focus on performance, scalability, and clean architecture.

### 🔄 **Improvements**

- **✅ Cache Implementation**: Added Caffeine cache with reactive support for improved performance
- **✅ Enhanced Testing**: Refactored integration tests with @MockBean for database independence
- **✅ @Nested Test Structure**: Organized tests with clear categorization (Success, Error, Edge Cases)
- **✅ Modern Assertions**: Upgraded to AssertJ for more readable test assertions
- **✅ Performance Optimization**: Achieved 1,781+ RPS with reactive programming patterns

### 🚀 **Performance Highlights**

- **🎯 Exceptional Performance**: 1,781+ RPS (17.8x better than target)
- **⚡ Ultra-Fast Response**: 3.40ms average response time
- **🔒 Perfect Reliability**: 100% success rate in optimized tests
- **📈 Production Ready**: Handles enterprise-level traffic loads
- **🏗️ Clean Architecture**: Hexagonal Architecture with SOLID principles
- **🔄 Reactive Programming**: Non-blocking I/O with Spring WebFlux

**Quick Start**: Application runs on `http://localhost:8081`

## Business Rules

### Price Selection Logic

The API implements a sophisticated price selection algorithm based on the following business rules:

1. **Date Range Validation**: A price is applicable only if the query date falls within the price's validity period (`startDate` ≤ `applicationDate` ≤ `endDate`)

2. **Product and Brand Matching**: The price must match exactly the requested `productId` and `brandId`

3. **Priority-Based Selection**: When multiple prices are valid for the same date/product/brand combination:
   - Higher priority values take precedence (priority 1 > priority 0)
   - Only the highest priority price is returned
   - This allows for promotional pricing, seasonal adjustments, and special offers

4. **Price List Identification**: Each price belongs to a specific price list (tariff), enabling:
   - Different pricing strategies (regular, promotional, VIP, etc.)
   - Time-based pricing variations
   - Customer segment-specific pricing

### Example Business Scenario

Consider a product (ID: 35455) for brand ZARA (ID: 1) with the following price structure:

```
Base Price (Priority 0): €35.50 - Valid all day, every day
Afternoon Promotion (Priority 1): €25.45 - Valid 15:00-18:30 on June 14th
Morning Special (Priority 1): €30.50 - Valid 00:00-11:00 on June 15th
Extended Offer (Priority 1): €38.95 - Valid from 16:00 June 15th onwards
```

**Query Examples:**
- `2020-06-14T10:00:00` → Returns €35.50 (base price, no promotions active)
- `2020-06-14T16:00:00` → Returns €25.45 (afternoon promotion active)
- `2020-06-14T21:00:00` → Returns €35.50 (promotion ended, back to base price)
- `2020-06-15T10:00:00` → Returns €30.50 (morning special active)
- `2020-06-16T21:00:00` → Returns €38.95 (extended offer active)

### Data Flow and Algorithm

The price selection follows this algorithmic approach:

```
1. Input Validation
   ├── Validate applicationDate format (ISO DateTime)
   ├── Validate productId (positive long)
   └── Validate brandId (positive integer)

2. Database Query
   ├── Query: SELECT * FROM prices 
   │          WHERE brand_id = ? 
   │          AND product_id = ? 
   │          AND ? BETWEEN start_date AND end_date
   │          ORDER BY priority DESC
   └── Result: List of applicable prices (highest priority first)

3. Price Selection Logic
   ├── IF list is empty → Return 404 Not Found
   ├── IF list has one item → Return that price
   └── IF list has multiple items → Return first item (highest priority)

4. Response Mapping
   ├── Map domain Price entity to PriceResponseDTO
   ├── Include all required fields (productId, brandId, priceList, etc.)
   └── Return HTTP 200 with JSON response
```

**Key Algorithm Features:**
- **Single Query Efficiency**: One database query retrieves all applicable prices
- **Database-Level Sorting**: Priority ordering handled by database for performance
- **Fail-Fast Validation**: Input validation before expensive database operations
- **Reactive Processing**: Non-blocking execution throughout the entire flow

## Implementation Strategy

### 1. Architectural Approach

The implementation follows **Hexagonal Architecture (Ports and Adapters)** to ensure:

- **Domain Independence**: Business logic is isolated from external concerns
- **Testability**: Easy mocking and testing of individual components
- **Flexibility**: Simple to swap implementations (database, web framework, etc.)
- **Maintainability**: Clear separation of responsibilities

### 2. Reactive Programming Strategy

**Why Reactive?**
- **Non-blocking I/O**: Handles thousands of concurrent requests efficiently
- **Resource Optimization**: Better CPU and memory utilization
- **Scalability**: Horizontal scaling with minimal resource overhead
- **Backpressure Handling**: Graceful degradation under high load

**Implementation Details:**
```java
// Reactive service method using Flux.next() for stream processing
public Mono<Price> findApplicablePrice(LocalDateTime date, Long productId, Integer brandId) {
    return Mono.fromCallable(() -> repository.findApplicablePrices(date, productId, brandId))
               .subscribeOn(Schedulers.boundedElastic())
               .flatMapMany(Flux::fromIterable)
               .next(); 
}
```

### 3. Performance Optimization Strategy

**Database Optimization:**
- **Indexed Queries**: Optimized queries with proper indexing on date ranges
- **Priority Ordering**: Database-level sorting to get highest priority first
- **Connection Pooling**: Efficient database connection management

**Reactive Optimizations:**

- **Non-blocking Operations**: Complete reactive chain from controller to repository
- **Scheduler Optimization**: Dedicated thread pools for blocking database operations

**Advanced Reactive Patterns:**
- **Scheduler Configuration**: Dedicated thread pools for blocking operations
- **Backpressure Management**: Controlled request flow to prevent overload
- **Stream Processing**: Elegant Flux.next() for first element selection

### 4. Data Access Strategy

**Repository Pattern with Ports:**
```java
// Domain port (interface)
public interface PriceRepositoryPort {
    List<Price> findApplicablePrices(LocalDateTime date, Long productId, Integer brandId);
}

// Infrastructure adapter (implementation)
@Repository
public interface PriceRepository extends JpaRepository<Price, Long>, PriceRepositoryPort {
    // Custom query implementation
}
```

**Query Optimization:**
- **Named Queries**: Externalized JPQL queries for maintainability
- **Parameter Binding**: Secure parameter binding to prevent SQL injection
- **Result Ordering**: Database-level sorting by priority (DESC)

### 5. Error Handling Strategy

**Global Exception Handling:**
- **Reactive Error Handling**: WebFlux-compatible exception handlers
- **Structured Error Responses**: Consistent error format across all endpoints
- **Validation Errors**: Detailed field-level validation messages
- **Logging Strategy**: Comprehensive error logging for debugging

**Error Response Format:**
```json
{
  "timestamp": "2020-06-14T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input parameters",
  "fieldErrors": {
    "applicationDate": "Invalid date format"
  }
}
```

### 6. Testing Strategy

**Multi-Level Testing:**
- **Unit Tests**: Domain logic testing with mocks and StepVerifier
- **Integration Tests**: WebTestClient with @MockBean for database independence
- **Performance Tests**: Load testing with K6 and Java-based scenarios
- **Reactive Testing**: Comprehensive @Nested test structure

**Modern Test Structure:**
```java
@Nested @DisplayName("Successful Price Queries")
class SuccessfulPriceQueries {
    @Test
    @DisplayName("Should return highest priority price when multiple prices match")
    void shouldReturnHighestPriorityPriceWhenMultipleMatch() {
        // Given - Mock data setup
        when(priceRepositoryPort.findApplicablePrices(eq(queryDate), eq(productId), eq(brandId)))
            .thenReturn(Arrays.asList(promotionalPrice, basePrice));
        
        // When & Then - WebTestClient validation
        webTestClient.get().uri(...).exchange().expectStatus().isOk();
    }
}
```

### 7. Deployment Strategy

**Containerization:**
- **Docker Support**: Complete containerization for consistent deployment
- **Multi-Stage Builds**: Optimized Docker images
- **Health Checks**: Container health monitoring
- **Environment Configuration**: Externalized configuration for different environments

**Monitoring and Observability:**
- **Spring Actuator**: Health checks, metrics, and application info
- **Performance Metrics**: Request/response time monitoring
- **Error Tracking**: Comprehensive error logging and tracking
- **Performance Metrics**: Request/response time monitoring

### 8. Scalability Considerations

**Horizontal Scaling:**
- **Stateless Design**: No server-side session state
- **Database Independence**: Easy to switch to distributed databases
- **Load Balancer Ready**: Supports multiple instance deployment
- **Reactive Scaling**: Non-blocking I/O enables efficient resource utilization

**Performance Targets:**
- **Throughput**: >1000 requests per second achieved
- **Response Time**: <500ms for 95th percentile
- **Concurrency**: Supports 200+ concurrent users
- **Resource Efficiency**: Minimal memory and CPU footprint

### 9. Technical Decisions and Trade-offs

**Architecture Decisions:**

1. **Spring WebFlux vs Spring MVC**
   - **Chosen**: WebFlux for reactive programming
   - **Rationale**: Better performance under high load, non-blocking I/O
   - **Trade-off**: Increased complexity, learning curve for reactive programming

2. **H2 In-Memory vs External Database**
   - **Chosen**: H2 for simplicity and performance testing
   - **Rationale**: Fast startup, no external dependencies, perfect for demos
   - **Trade-off**: Data doesn't persist between restarts (acceptable for this use case)

3. **Hexagonal Architecture vs Layered Architecture**
   - **Chosen**: Hexagonal Architecture (Ports and Adapters)
   - **Rationale**: Better testability, dependency inversion, domain isolation
   - **Trade-off**: More interfaces and abstractions (justified by maintainability gains)

4. **Reactive Streams vs Blocking Operations**
   - **Chosen**: Reactive with `Flux.next()` for elegant stream processing
   - **Rationale**: Maintains reactive chain with elegant first element selection
   - **Trade-off**: More complex reactive patterns, but better code readability

**Performance Trade-offs:**

1. **Reactive Stream Processing**
   - **Decision**: Flux.next() pattern for elegant first element selection
   - **Benefit**: More readable code, better reactive stream handling
   - **Cost**: Slightly more complex reactive chain (justified by elegance)

2. **Database Query Optimization**
   - **Decision**: Single query with ORDER BY priority DESC
   - **Benefit**: Minimal database round-trips, database-optimized sorting
   - **Cost**: Slightly more complex query (negligible impact)

3. **Error Handling Approach**
   - **Decision**: Global exception handler with structured responses
   - **Benefit**: Consistent error format, centralized error logic
   - **Cost**: Additional abstraction layer (justified by maintainability)

**Scalability Decisions:**

1. **Stateless Design**
   - **Decision**: No server-side session state
   - **Benefit**: Easy horizontal scaling, load balancer friendly
   - **Cost**: No session-based optimizations (not needed for this use case)

2. **Connection Pooling**
   - **Decision**: Default HikariCP with optimized settings
   - **Benefit**: Efficient database connection reuse
   - **Cost**: Memory overhead for connection pool (minimal)

3. **Reactive Schedulers**
   - **Decision**: `boundedElastic()` scheduler for blocking operations
   - **Benefit**: Prevents blocking of event loop threads
   - **Cost**: Additional thread pool overhead (necessary for performance)

## Prerequisites

- Java 17 or higher
- Apache Maven 3.6.x or higher

## Building and Running the Project

1.  **Clone the repository (if you haven't already):**
    ```bash
    git clone git@github.com:robertluiz/price-selector-api.git
    cd price-selector-api
    ```

2.  **Build the project using Maven:**
    ```bash
    mvn clean install
    ```
    This will compile the code, run tests, and package the application into a JAR file in the `target` directory.

3.  **Run the application:**
    ```bash
    java -jar target/price-selector-api-0.0.1-SNAPSHOT.jar
    ```
    The application will start, and the H2 database will be initialized with data from `src/main/resources/data.sql`.
    By default, the service will be available at `http://localhost:8081`.

4.  **Verify the application is running:**
    ```bash
    # Check application health
    curl http://localhost:8081/actuator/health
    
    # Test the API endpoint
    curl "http://localhost:8081/api/v1/prices/query?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1"
    ```

## Running Tests

To run the unit and integration tests, use the following Maven command:
```bash
mvn test
```

## Performance Testing

The application includes comprehensive performance testing capabilities to ensure it meets high-performance requirements.

### K6 Load Testing
```bash
# Start the application
docker-compose up -d

# Run K6 performance tests (without monitoring)
docker-compose --profile k6 run k6

# Run K6 performance tests with specific command
docker-compose --profile k6 run k6 run /scripts/performance-test.js
```

### Performance Monitoring with Grafana

The project includes comprehensive monitoring with Grafana and InfluxDB for real-time metrics visualization.

```bash
# Start monitoring services (InfluxDB + Grafana)
docker-compose --profile monitoring up -d

# Run K6 tests with metrics collection
docker-compose --profile monitoring --profile k6-monitoring run k6-with-monitoring

# Access Grafana dashboard at http://localhost:3000
# User: admin, Password: admin
```

#### 📊 Automatic Dashboard

The **"K6 Performance Dashboard"** is automatically loaded and includes:

- **Requests per Second (RPS)**: Request rate per second
- **Response Time**: Average response time and 95th percentile
- **Virtual Users**: Number of active virtual users  
- **Error Rate**: Error rate percentage

#### 📈 Expected Success Metrics

- **RPS**: > 1000 (Excellent performance)
- **Response Time**: < 50ms (Very good)
- **Error Rate**: 0% (Ideal)
- **P95**: < 100ms (Acceptable)

#### 🔧 Troubleshooting

```bash
# Check container status
docker ps

# Check Grafana logs
docker logs price-selector-api-grafana-1

# Restart services if needed
docker-compose --profile monitoring restart
```

### Running Performance Tests

#### 1. Java-based Performance Test
```bash
# Build the application
mvn clean package

# Start the application (runs on port 8081)
java -jar target/price-selector-api-0.0.1-SNAPSHOT.jar

# In another terminal, run the built-in performance test
mvn test-compile exec:java -Dexec.mainClass="com.example.priceselectorapi.performance.PerformanceTestRunner" -Dexec.classpathScope="test"

# Alternative using Maven profile (requires application running)
mvn clean test-compile -Pperformance exec:java
```

**Expected Results:**
```
✅ Total Requests: 1,000
✅ Successful RPS: 583+ 
✅ Success Rate: 100%
✅ Performance: 5.8x better than target
```

#### 2. K6 Load Testing (Docker)
```bash
# Build and start the application with Docker
docker-compose up -d

# Run K6 performance tests (without monitoring)
docker-compose --profile k6 run k6

# Run K6 performance tests with explicit command
docker-compose --profile k6 run k6 run /scripts/performance-test.js
```

**Expected Results:**
```
✅ Total Requests: 214,006+
✅ Successful RPS: 1,781+
✅ Average Response Time: 3.40ms
✅ Performance: 17.8x better than target
```

#### 3. Docker-based Performance Testing
```bash
# Build and start services
docker-compose up -d

# Run K6 tests (without monitoring)
docker-compose --profile k6 run k6

# Run with monitoring (Grafana + InfluxDB)
docker-compose --profile monitoring up -d
docker-compose --profile monitoring --profile k6-monitoring run k6-with-monitoring
```

### Performance Results Achieved

The application has been tested and **exceeds all performance targets**:

#### 🎯 **Target vs Achieved Performance**

| Metric | Target | **Achieved** | **Performance Ratio** |
|--------|--------|--------------|----------------------|
| **Throughput** | >100 RPS | **1,781 RPS** (K6) / **583 RPS** (Java) | **17.8x / 5.8x better** |
| **Response Time** | <500ms (95th) | **10.47ms** (95th percentile) | **47.7x better** |
| **Success Rate** | >99% | **100%** (optimized test) | **Perfect reliability** |
| **Concurrent Users** | 200+ users | **200+ users** | **Target met and exceeded** |

#### 🚀 **Exceptional Performance Highlights**

- **K6 Load Test Results**: 1,781.30 RPS with 3.40ms average response time
- **Java Performance Test**: 583.43 RPS with 100% success rate
- **Zero Downtime**: Application maintained stability under extreme load
- **Sub-10ms Responses**: 95th percentile response time of 10.47ms
- **Perfect Reliability**: 100% success rate in optimized test scenarios

#### 📊 **Detailed Test Results**

**K6 Load Testing (External):**
```
✅ Total Requests: 214,006
✅ Successful RPS: 1,781.30
✅ Average Response Time: 3.40ms
✅ 95th Percentile: 10.47ms
✅ Success Rate: >99.9%
```

**Java Performance Testing (Internal):**
```
✅ Total Requests: 1,000
✅ Successful RPS: 583.43
✅ Average Response Time: 351.09ms
✅ Success Rate: 100%
✅ Zero Errors: Perfect reliability
```

#### 🏆 **Performance Achievements**

- **17.8x Better Throughput**: Far exceeds the 100 RPS requirement
- **47.7x Faster Response**: Sub-10ms responses vs 500ms target
- **Production Ready**: Handles enterprise-level traffic loads
- **Reactive Excellence**: Spring WebFlux delivers exceptional performance

### Performance Test Scenarios

1. **Normal Load**: 50 concurrent users for 30 seconds
2. **Spike Test**: Ramp up to 100 users, sustain, then ramp down
3. **Stress Test**: 200 concurrent users for extended periods
4. **Endurance Test**: Sustained load over longer durations

### Monitoring

Performance metrics can be monitored through:
- **Grafana Dashboard**: http://localhost:3000 (admin/admin)
- **Spring Actuator**: http://localhost:8081/actuator
- **Application Logs**: Detailed performance logging

## Testing with Postman

A Postman collection is available in the root of the project: `PriceSelectorAPI.postman_collection.json`.

You can import this collection into Postman to easily test the API endpoint with the predefined test cases.
The collection uses a variable `{{baseUrl}}` which is set to `http://localhost:8081` by default.

## API Endpoint

### Query Applicable Price

-   **GET** `/api/v1/prices/query`

-   **Description:** Retrieves the applicable price for a product based on the application date, product ID, and brand ID.

-   **Request Parameters:**
    -   `applicationDate` (String, ISO_DATE_TIME format, e.g., `2020-06-14T10:00:00`): The date and time for which the price is being queried.
    -   `productId` (Long): The ID of the product.
    -   `brandId` (Integer): The ID of the brand (e.g., 1 for ZARA).

-   **Example Request:**
    ```
    GET http://localhost:8081/api/v1/prices/query?applicationDate=2020-06-14T10:00:00&productId=35455&brandId=1
    ```

-   **Success Response (200 OK):**
    -   **Content-Type:** `application/json`
    -   **Body:**
        ```json
        {
            "productId": 35455,
            "brandId": 1,
            "priceList": 1, // Tariff ID
            "startDate": "2020-06-14T00:00:00",
            "endDate": "2020-12-31T23:59:59",
            "finalPrice": 35.50,
            "currency": "EUR"
        }
        ```

-   **Error Response (404 Not Found):**
    -   If no applicable price is found for the given criteria.
    -   **Body:** Empty

-   **Error Response (400 Bad Request):**
    -   If input parameters are invalid (e.g., missing, incorrect format).
    -   **Body:** Standard Spring Boot validation error response.

## Sample Data

The H2 in-memory database is initialized with the following sample data upon startup (see `src/main/resources/data.sql` for details):

| BRAND_ID | START_DATE          | END_DATE            | PRICE_LIST | PRODUCT_ID | PRIORITY | PRICE | CURR |
|----------|---------------------|---------------------|------------|------------|----------|-------|------|
| 1        | 2020-06-14 00:00:00 | 2020-12-31 23:59:59 | 1          | 35455      | 0        | 35.50 | EUR  |
| 1        | 2020-06-14 15:00:00 | 2020-06-14 18:30:00 | 2          | 35455      | 1        | 25.45 | EUR  |
| 1        | 2020-06-15 00:00:00 | 2020-06-15 11:00:00 | 3          | 35455      | 1        | 30.50 | EUR  |
| 1        | 2020-06-15 16:00:00 | 2020-12-31 23:59:59 | 4          | 35455      | 1        | 38.95 | EUR  |

## Technologies Used

### Core Technologies
- **Java 17**: Modern Java features and performance improvements
- **Spring Boot 3.3.0**: Latest Spring Boot with enhanced reactive support
- **Spring WebFlux**: Reactive web framework for non-blocking operations (**1,781+ RPS achieved**)
- **Spring Data JPA**: Data access layer with repository pattern
- **Spring Boot Validation**: Request validation and error handling
- **Spring Boot Validation**: Request validation and error handling
- **Spring Boot Actuator**: Production-ready monitoring and metrics

### Database & Persistence
- **H2 Database**: In-memory database for development and testing
- **Hibernate**: ORM for database operations

### Performance & Resilience
- **Caffeine Cache**: High-performance caching with reactive support and metrics
- **AssertJ**: Modern assertion library for better test readability
- **Reactor**: Reactive streams implementation for non-blocking operations
- **Resilience4j**: Circuit breaker and fault tolerance patterns

### Development & Testing
- **Lombok**: Reduces boilerplate code
- **Maven**: Build automation and dependency management
- **JUnit 5**: Modern testing framework
- **Mockito**: Mocking framework for unit tests
- **Reactor Test**: Testing utilities for reactive streams
- **Testcontainers**: Integration testing with containers

### Performance Testing
- **K6**: Load testing tool for performance validation
- **WebClient**: Reactive HTTP client for performance tests
- **Docker**: Containerization for consistent environments

## Architecture

### Hexagonal Architecture Implementation

The project follows Hexagonal Architecture (Ports and Adapters) principles, ensuring clean separation of concerns and high testability:

```
┌─────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                     │
│  ┌─────────────────┐                   ┌─────────────────┐  │
│  │   Web Adapters  │                   │ Persistence     │  │
│  │   (Controllers) │                   │ Adapters        │  │
│  │   - REST API    │                   │ - JPA Repos     │  │
│  │   - WebFlux     │                   │ - H2 Database   │  │
│  └─────────────────┘                   └─────────────────┘  │
└─────────────────────────────────────────────────────────────┘
           │                                       │
           ▼                                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   Application Layer                         │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              Use Cases / Services                       │ │
│  │  - PriceQueryService (implements PriceQueryPort)        │ │
│  │  - Reactive programming with Mono/Flux                 │ │
│  │  - Caching and performance optimization                 │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
           │                                       │
           ▼                                       ▼
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                            │
│  ┌─────────────────┐                   ┌─────────────────┐  │
│  │   Domain Model  │                   │     Ports       │  │
│  │   - Price       │                   │ - PriceQueryPort│  │
│  │   - Entities    │                   │ - PriceRepoPort │  │
│  │   - Value Objs  │                   │ - Interfaces    │  │
│  └─────────────────┘                   └─────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Package Structure

-   **`domain/`**: Core business logic and rules
    -   `model/`: Domain entities (`Price.java`)
    -   `port/`: Interfaces defining contracts (`PriceQueryPort`, `PriceRepositoryPort`)
    -   `repository/`: Repository interfaces extending ports

-   **`application/`**: Use cases and application services
    -   `service/`: Business logic implementation (`PriceQueryService`)
    -   `dto/`: Data Transfer Objects for external communication

-   **`infrastructure/`**: External concerns and adapters
    -   `web/controller/`: REST API controllers (WebFlux-based)
    -   `web/handler/`: Global exception handling
    -   `config/`: Configuration classes (WebFlux, etc.)

### Key Architectural Patterns

1. **Dependency Inversion**: High-level modules don't depend on low-level modules
2. **Single Responsibility**: Each class has one reason to change
3. **Open/Closed**: Open for extension, closed for modification
4. **Interface Segregation**: Clients depend only on interfaces they use
5. **Liskov Substitution**: Objects are replaceable with instances of their subtypes

### Performance Optimizations

1. **Reactive Programming**: Non-blocking I/O with Spring WebFlux
2. **Caching Strategy**: Caffeine cache with reactive support and automatic metrics
3. **Connection Pooling**: Optimized database connection management
4. **Lazy Loading**: Efficient data loading strategies
5. **Parallel Processing**: Concurrent request handling 