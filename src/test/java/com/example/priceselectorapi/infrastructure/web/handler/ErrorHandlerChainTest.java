package com.example.priceselectorapi.infrastructure.web.handler;

import com.example.priceselectorapi.application.dto.PriceResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import jakarta.validation.ConstraintViolationException;
import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Error Handler Chain Tests")
class ErrorHandlerChainTest {

    private ValidationErrorHandler validationErrorHandler;
    private DatabaseErrorHandler databaseErrorHandler;
    private GenericErrorHandler genericErrorHandler;
    private List<ErrorHandler<? extends Throwable>> errorHandlers;

    @BeforeEach
    void setUp() {
        validationErrorHandler = new ValidationErrorHandler();
        databaseErrorHandler = new DatabaseErrorHandler();
        genericErrorHandler = new GenericErrorHandler();
        
        errorHandlers = List.of(validationErrorHandler, databaseErrorHandler, genericErrorHandler);
    }

    @Nested
    @DisplayName("Validation Error Handler Tests")
    class ValidationErrorHandlerTests {

        @Test
        @DisplayName("Should handle ConstraintViolationException")
        void shouldHandleConstraintViolationException() {
            ConstraintViolationException exception = new ConstraintViolationException("Validation failed", null);

            assertThat(validationErrorHandler.canHandle(exception)).isTrue();
            assertThat(validationErrorHandler.getOrder()).isEqualTo(1);

            StepVerifier.create(validationErrorHandler.handle(exception))
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(response.getBody()).isNull();
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle WebExchangeBindException")
        void shouldHandleWebExchangeBindException() throws NoSuchMethodException {
            // Create a valid MethodParameter for the exception
            Method method = ErrorHandlerChainTest.class.getDeclaredMethod("mockMethod", String.class);
            MethodParameter methodParameter = new MethodParameter(method, 0);
            
            // Create a BindingResult
            BindingResult bindingResult = new BeanPropertyBindingResult("target", "objectName");
            
            WebExchangeBindException exception = new WebExchangeBindException(methodParameter, bindingResult);

            assertThat(validationErrorHandler.canHandle(exception)).isTrue();

            StepVerifier.create(validationErrorHandler.handle(exception))
                    .assertNext(response -> 
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException")
        void shouldHandleIllegalArgumentException() {
            IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

            assertThat(validationErrorHandler.canHandle(exception)).isTrue();

            StepVerifier.create(validationErrorHandler.handle(exception))
                    .assertNext(response -> 
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should not handle RuntimeException")
        void shouldNotHandleRuntimeException() {
            RuntimeException exception = new RuntimeException("Generic error");

            assertThat(validationErrorHandler.canHandle(exception)).isFalse();
        }
    }

    @Nested
    @DisplayName("Database Error Handler Tests")
    class DatabaseErrorHandlerTests {

        @Test
        @DisplayName("Should handle DataAccessException")
        void shouldHandleDataAccessException() {
            DataAccessException exception = new DataAccessException("Database error") {};

            assertThat(databaseErrorHandler.canHandle(exception)).isTrue();
            assertThat(databaseErrorHandler.getOrder()).isEqualTo(2);

            StepVerifier.create(databaseErrorHandler.handle(exception))
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                        assertThat(response.getBody()).isNull();
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should not handle IllegalArgumentException")
        void shouldNotHandleIllegalArgumentException() {
            IllegalArgumentException exception = new IllegalArgumentException("Validation error");

            assertThat(databaseErrorHandler.canHandle(exception)).isFalse();
        }
    }

    @Nested
    @DisplayName("Generic Error Handler Tests")
    class GenericErrorHandlerTests {

        @Test
        @DisplayName("Should handle any Throwable")
        void shouldHandleAnyThrowable() {
            RuntimeException exception = new RuntimeException("Unexpected error");

            assertThat(genericErrorHandler.canHandle(exception)).isTrue();
            assertThat(genericErrorHandler.getOrder()).isEqualTo(999);

            StepVerifier.create(genericErrorHandler.handle(exception))
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                        assertThat(response.getBody()).isNull();
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle custom exception")
        void shouldHandleCustomException() {
            Exception exception = new Exception("Custom error");

            assertThat(genericErrorHandler.canHandle(exception)).isTrue();

            StepVerifier.create(genericErrorHandler.handle(exception))
                    .assertNext(response -> 
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Handler Chain Priority Tests")
    class HandlerChainPriorityTests {

        @Test
        @DisplayName("Should find appropriate handler in correct order")
        void shouldFindAppropriateHandlerInCorrectOrder() {
            ConstraintViolationException validationException = new ConstraintViolationException("Validation", null);
            DataAccessException databaseException = new DataAccessException("Database") {};
            RuntimeException genericException = new RuntimeException("Generic");

            ErrorHandler<?> validationHandler = findHandler(validationException);
            ErrorHandler<?> databaseHandler = findHandler(databaseException);
            ErrorHandler<?> genericHandler = findHandler(genericException);

            assertThat(validationHandler).isInstanceOf(ValidationErrorHandler.class);
            assertThat(databaseHandler).isInstanceOf(DatabaseErrorHandler.class);
            assertThat(genericHandler).isInstanceOf(GenericErrorHandler.class);

            assertThat(validationHandler.getOrder()).isLessThan(databaseHandler.getOrder());
            assertThat(databaseHandler.getOrder()).isLessThan(genericHandler.getOrder());
        }

        private ErrorHandler<?> findHandler(Throwable throwable) {
            return errorHandlers.stream()
                    .sorted((h1, h2) -> Integer.compare(h1.getOrder(), h2.getOrder()))
                    .filter(handler -> handler.canHandle(throwable))
                    .findFirst()
                    .orElse(null);
        }
    }

    @Nested
    @DisplayName("Liskov Substitution Tests")
    class LiskovSubstitutionTests {

        @Test
        @DisplayName("All handlers should be substitutable")
        void allHandlersShouldBeSubstitutable() {
            Throwable validationError = new IllegalArgumentException("Invalid");
            Throwable databaseError = new DataAccessException("DB Error") {};
            Throwable genericError = new RuntimeException("Generic");

            List<Throwable> errors = List.of(validationError, databaseError, genericError);
            List<ErrorHandler<? extends Throwable>> handlers = List.of(
                validationErrorHandler, databaseErrorHandler, genericErrorHandler
            );

            for (Throwable error : errors) {
                ErrorHandler<? extends Throwable> handler = handlers.stream()
                        .sorted((h1, h2) -> Integer.compare(h1.getOrder(), h2.getOrder()))
                        .filter(h -> h.canHandle(error))
                        .findFirst()
                        .orElse(null);

                assertThat(handler).isNotNull();
                
                @SuppressWarnings("unchecked")
                Mono<ResponseEntity<PriceResponseDTO>> result = 
                    ((ErrorHandler<Throwable>) handler).handle(error);
                
                StepVerifier.create(result)
                        .assertNext(response -> assertThat(response.getStatusCode()).isNotNull())
                        .verifyComplete();
            }
        }
    }
    
    // Mock method for creating MethodParameter
    private void mockMethod(String parameter) {
        // This method is only used for creating MethodParameter in tests
    }
} 