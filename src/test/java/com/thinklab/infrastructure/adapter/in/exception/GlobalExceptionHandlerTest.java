package com.thinklab.infrastructure.adapter.in.exception;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpRequest<?> request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = Mockito.mock(HttpRequest.class);
        Mockito.when(request.getPath()).thenReturn("/api/v1/companies/test");
    }

    @Test
    @DisplayName("Should map NoSuchElementException to 404 Not Found problem details")
    void testNoSuchElementExceptionMapping() {
        NoSuchElementException exception = new NoSuchElementException("Company not found with ID: 123");
        HttpResponse<GlobalExceptionHandler.Rfc7807Problem> response = exceptionHandler.handle(request, exception);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());

        GlobalExceptionHandler.Rfc7807Problem body = response.body();
        assertNotNull(body);
        assertEquals(404, body.status());
        assertEquals("Not Found", body.title());
        assertEquals("Company not found with ID: 123", body.detail());
        assertEquals("/api/v1/companies/test", body.instance());
    }

    @Test
    @DisplayName("Should map IllegalArgumentException to 422 Unprocessable Entity problem details")
    void testIllegalArgumentExceptionMapping() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid company status");
        HttpResponse<GlobalExceptionHandler.Rfc7807Problem> response = exceptionHandler.handle(request, exception);

        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatus());

        GlobalExceptionHandler.Rfc7807Problem body = response.body();
        assertNotNull(body);
        assertEquals(422, body.status());
        assertEquals("Unprocessable Entity - Domain Rule Violated", body.title());
        assertEquals("Invalid company status", body.detail());
    }

    @Test
    @DisplayName("Should map ConstraintViolationException to 400 Bad Request")
    void testConstraintViolationExceptionMapping() {
        ConstraintViolationException exception = new ConstraintViolationException("Validation failed", Collections.emptySet());
        HttpResponse<GlobalExceptionHandler.Rfc7807Problem> response = exceptionHandler.handle(request, exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());

        GlobalExceptionHandler.Rfc7807Problem body = response.body();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request - Validation Failed", body.title());
    }

    @Test
    @DisplayName("Should map generic unexpected Exception to 500 Internal Server Error")
    void testGenericExceptionMapping() {
        RuntimeException exception = new RuntimeException("Unexpected internal failure");
        HttpResponse<GlobalExceptionHandler.Rfc7807Problem> response = exceptionHandler.handle(request, exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());

        GlobalExceptionHandler.Rfc7807Problem body = response.body();
        assertNotNull(body);
        assertEquals(500, body.status());
        assertEquals("Internal Server Error", body.title());
    }
}
