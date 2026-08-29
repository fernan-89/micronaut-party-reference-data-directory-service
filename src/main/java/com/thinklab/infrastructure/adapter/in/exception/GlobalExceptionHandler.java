package com.thinklab.infrastructure.adapter.in.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Global Exception Handler enforcing the RFC 7807 (Problem Details) standard.
 * Uses name-based inspection to completely eliminate strict bytecode classloader coupling
 * with optional validation modules, ensuring high availability and zero ClassNotFound exceptions.
 */
@Singleton
@Requires(classes = {Throwable.class, ExceptionHandler.class})
public class GlobalExceptionHandler implements ExceptionHandler<Throwable, HttpResponse<GlobalExceptionHandler.Rfc7807Problem>> {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String PROBLEM_CONTENT_TYPE = "application/problem+json";

    @Override
    public HttpResponse<Rfc7807Problem> handle(HttpRequest request, Throwable exception) {
        String exceptionClassName = exception.getClass().getName();

        // 1. Business Validation Failures (Name-based resolution to prevent classloader loading issues)
        if (exceptionClassName.contains("ConstraintViolationException")) {
            log.warn("[BUSINESS FAILURE] Validation error on endpoint {}", request.getPath());

            List<String> violations;
            try {
                // Reflection fallback to extract constraint messages safely if present
                var violationsMethod = exception.getClass().getMethod("getConstraintViolations");
                var rawViolations = (Iterable<?>) violationsMethod.invoke(exception);
                java.util.List<String> extracted = new java.util.ArrayList<>();
                for (Object v : rawViolations) {
                    extracted.add(v.toString());
                }
                violations = extracted;
            } catch (Exception e) {
                violations = Collections.singletonList(exception.getMessage());
            }

            Rfc7807Problem problem = new Rfc7807Problem(
                    URI.create("https://thinklab.com/probs/validation-error"),
                    "Bad Request - Validation Failed",
                    HttpStatus.BAD_REQUEST.getCode(),
                    "The payload contains invalid parameters.",
                    request.getPath(),
                    violations,
                    Instant.now()
            );
            return HttpResponse.badRequest(problem).contentType(PROBLEM_CONTENT_TYPE);
        }

        // 2. Business Domain Failures (Illegal Arguments or State Mutations)
        if (exception instanceof IllegalArgumentException || exception instanceof IllegalStateException) {
            log.warn("[BUSINESS FAILURE] Domain rule violation: {}", exception.getMessage());
            Rfc7807Problem problem = new Rfc7807Problem(
                    URI.create("https://thinklab.com/probs/business-rule-violation"),
                    "Unprocessable Entity - Domain Rule Violated",
                    HttpStatus.UNPROCESSABLE_ENTITY.getCode(),
                    exception.getMessage(),
                    request.getPath(),
                    null,
                    Instant.now()
            );
            return HttpResponse.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem).contentType(PROBLEM_CONTENT_TYPE);
        }

        // 3. Infrastructure & Unhandled Failures
        log.error("[INFRASTRUCTURE FAILURE] Unexpected system error during request to {}", request.getPath(), exception);
        Rfc7807Problem problem = new Rfc7807Problem(
                URI.create("https://thinklab.com/probs/internal-server-error"),
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                "An unexpected system failure occurred. Our SRE team has been notified.",
                request.getPath(),
                null,
                Instant.now()
        );
        return HttpResponse.serverError(problem).contentType(PROBLEM_CONTENT_TYPE);
    }

    /**
     * RFC 7807 Compliant Problem Details DTO.
     */
    @Serdeable
    public record Rfc7807Problem(
            URI type,
            String title,
            int status,
            String detail,
            String instance,
            List<String> violations,
            Instant timestamp
    ) {}
}