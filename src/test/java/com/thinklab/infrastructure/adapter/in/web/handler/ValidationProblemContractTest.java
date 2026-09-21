package com.thinklab.infrastructure.adapter.in.web.handler;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * HTTP-level contract test: a request body that fails bean validation must come back as the platform's
 * RFC 7807 problem (with {@code error_code}), not Micronaut's default validation envelope. Found by the
 * first live end-to-end run — the unit tests only exercised the handler directly.
 */
@MicronautTest
class ValidationProblemContractTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    @DisplayName("an invalid body is reported as an RFC 7807 problem with ERR-VALIDATION-00400")
    void invalidBodyIsRfc7807() {
        HttpRequest<String> request = HttpRequest.POST("/party-reference-data-directory/v1/initiate", "{\"corporateName\":\"\",\"tradeName\":\"t\",\"taxIdentifier\":\"12345678000190\",\"billing\":{\"billingEmail\":\"b@t.com\",\"currency\":\"USD\",\"taxRegime\":\"SIMPLES\"}}")
                .contentType("application/json").header("X-Executor", "e2e");

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().exchange(request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        Map<?, ?> problem = ex.getResponse().getBody(Map.class).orElse(null);
        assertNotNull(problem, "the 400 must carry a problem document");
        assertEquals(400, problem.get("status"));
        assertEquals("ERR-VALIDATION-00400", problem.get("error_code"));
        assertNotNull(problem.get("detail"));
    }
}
