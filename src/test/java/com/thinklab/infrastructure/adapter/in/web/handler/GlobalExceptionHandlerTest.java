package com.thinklab.infrastructure.adapter.in.web.handler;

import com.thinklab.domain.exception.InvalidOrganisationStatusException;
import com.thinklab.domain.exception.OrganisationNotFoundException;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpRequest<?> request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = Mockito.mock(HttpRequest.class);
        HttpHeaders headers = Mockito.mock(HttpHeaders.class);
        Mockito.when(request.getPath()).thenReturn("/party-reference-data-directory/v1/test");
        Mockito.when(request.getAttribute(Mockito.eq("traceId"), Mockito.eq(String.class))).thenReturn(Optional.empty());
        Mockito.when(request.getHeaders()).thenReturn(headers);
        Mockito.when(headers.get("X-Trace-Id")).thenReturn(null);
    }

    @Test
    @DisplayName("Should map OrganisationNotFoundException to 404 Not Found problem details with error_code")
    void testOrganisationNotFoundExceptionMapping() {
        OrganisationNotFoundException exception = new OrganisationNotFoundException("Organisation not found with ID: 123");
        HttpResponse<Map<String, Object>> response = exceptionHandler.handle(request, exception);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());

        Map<String, Object> body = response.body();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("ERR-ORG-00404", body.get("error_code"));
        assertEquals("Organisation not found with ID: 123", body.get("detail"));
        assertEquals("/party-reference-data-directory/v1/test", body.get("instance"));
    }

    @Test
    @DisplayName("Should map InvalidOrganisationStatusException to 409 Conflict problem details")
    void testInvalidOrganisationStatusExceptionMapping() {
        InvalidOrganisationStatusException exception = new InvalidOrganisationStatusException("Illegal state transition");
        HttpResponse<Map<String, Object>> response = exceptionHandler.handle(request, exception);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatus());

        Map<String, Object> body = response.body();
        assertNotNull(body);
        assertEquals(409, body.get("status"));
        assertEquals("ERR-ORG-00409", body.get("error_code"));
    }

    @Test
    @DisplayName("Should map ConstraintViolationException to 400 Bad Request")
    void testConstraintViolationExceptionMapping() {
        ConstraintViolationException exception = new ConstraintViolationException("Validation failed", Collections.emptySet());
        HttpResponse<Map<String, Object>> response = exceptionHandler.handle(request, exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());

        Map<String, Object> body = response.body();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("ERR-VALIDATION-00400", body.get("error_code"));
    }

    @Test
    @DisplayName("Should map generic unexpected Exception to 500 Internal Server Error")
    void testGenericExceptionMapping() {
        RuntimeException exception = new RuntimeException("Unexpected internal failure");
        HttpResponse<Map<String, Object>> response = exceptionHandler.handle(request, exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());

        Map<String, Object> body = response.body();
        assertNotNull(body);
        assertEquals(500, body.get("status"));
        assertEquals("ERR-INTERNAL-00500", body.get("error_code"));
    }
}
