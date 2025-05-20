# Price Selector API

## Description

This is a Spring Boot application that provides a REST API to query product prices for a given brand, product ID, and application date. It uses an in-memory H2 database initialized with sample data.

The service determines the applicable price based on date ranges and a priority field. If multiple prices match the criteria, the one with the highest priority is selected.

This project was developed as a technical assessment.

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
    By default, the service will be available at `http://localhost:8080`.

## Running Tests

To run the unit and integration tests, use the following Maven command:
```bash
mvn test
```

## Testing with Postman

A Postman collection is available in the root of the project: `PriceSelectorAPI.postman_collection.json`.

You can import this collection into Postman to easily test the API endpoint with the predefined test cases.
The collection uses a variable `{{baseUrl}}` which is set to `http://localhost:8080` by default.

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
    GET http://localhost:8080/api/v1/prices/query?applicationDate=2020-06-14T10:00:00&productId=35455&brandId=1
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

- Java 17
- Spring Boot 3.3.0
    - Spring Web
    - Spring Data JPA
    - Spring Boot Starter Validation
- H2 Database Engine (in-memory)
- Lombok
- Maven (build tool)
- JUnit 5 (testing)
- Mockito (mocking for tests)

## Project Structure Notes

The project is organized into the following main packages, adhering to Hexagonal Architecture principles:

-   **`com.example.priceselectorapi.domain`**: The core of the application.
    -   `model/`: Contains the domain entities (e.g., `Price.java`).
    -   `repository/`: Defines the repository ports (interfaces, e.g., `PriceRepository.java`) that the application layer uses to interact with data, abstracting the persistence mechanism.
-   **`com.example.priceselectorapi.application`**: Contains the application services and DTOs.
    -   `service/`: Implements the use cases / application logic (e.g., `PriceQueryService.java`), orchestrating calls to domain objects and repositories.
    -   `dto/`: Data Transfer Objects (e.g., `PriceResponseDTO.java`) used for communication between the web adapter and the application service.
-   **`com.example.priceselectorapi.infrastructure`**: Contains the adapters that implement the ports defined in the domain or interact with external systems.
    -   `web/controller/`: Implements the primary adapter (REST API endpoint, e.g., `PriceController.java`) which translates HTTP requests into calls to the application service.
    -   `persistence/`: While not explicitly created as a separate package for adapters in this simple project (Spring Data JPA handles much of the boilerplate), the `PriceRepository` interface itself, when implemented by Spring Data JPA, acts as the secondary adapter for persistence.
-   **`src/main/resources`**:
    -   `application.properties`: Spring Boot configuration.
    -   `data.sql`: Script to initialize the H2 in-memory database with sample data.
    -   `META-INF/jpa-named-queries.properties`: Contains custom JPQL queries used by Spring Data JPA repositories. This helps keep queries externalized from the Java code.

The project aims to follow principles of Hexagonal Architecture (Ports and Adapters), promoting a separation of concerns between the core business logic (domain and application layers) and the infrastructure details (web, persistence). 