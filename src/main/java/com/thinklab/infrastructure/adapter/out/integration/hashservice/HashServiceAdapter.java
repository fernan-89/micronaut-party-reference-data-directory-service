package com.thinklab.infrastructure.adapter.out.integration.hashservice;

import com.thinklab.domain.port.HashServicePort;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Outbound Adapter for the external Hash-Service.
 * Implements the Domain Port, ensuring that Micronaut-specific HTTP client
 * details do not leak into the Application or Domain layers.
 */
@Singleton
public class HashServiceAdapter implements HashServicePort {

    private static final Logger log = LoggerFactory.getLogger(HashServiceAdapter.class);

    private final HashApiClient apiClient;

    public HashServiceAdapter(HashApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Mono<UUID> generateSovereignId(String context) {
        log.debug("[INTEGRATION] Requesting secure Sovereign ID from Hash-Service. Context: {}", context);

        return apiClient.generateId(context)
                .doOnSuccess(id -> log.debug("[INTEGRATION] Successfully retrieved Sovereign ID: {}", id))
                .doOnError(error -> log.error("[INTEGRATION FAILURE] Failed to fetch Sovereign ID from Hash-Service", error))
                // SRE: Translating infrastructure failures to prevent internal stack traces from leaking
                .onErrorMap(error -> new IllegalStateException("Dependency Failure: Hash-Service is currently unavailable", error));
    }

    @Override
    public Mono<String> hashSensitiveData(String rawData) {
        log.debug("[INTEGRATION] Requesting cryptographic hash for sensitive data from Hash-Service.");

        return apiClient.hashData(rawData)
                .doOnError(error -> log.error("[INTEGRATION FAILURE] Failed to hash sensitive data", error))
                .onErrorMap(error -> new IllegalStateException("Dependency Failure: Hash-Service cryptographic operations are unavailable", error));
    }
}

/**
 * Declarative Micronaut HTTP Client for the Hash-Service API.
 * Package-private visibility strictly encapsulates this integration detail within the adapter.
 * The 'id' maps to the configuration in application.yml for dynamic resolution.
 */
@Client(id = "hash-service", path = "/api/v1/crypto")
interface HashApiClient {

    /**
     * Fetches a cryptographically secure UUID v4 (Blake3 based) from the Hash-Service.
     *
     * @param context Operational context for audit trails.
     * @return A Mono emitting the UUID.
     */
    @Get("/uuid")
    Mono<UUID> generateId(@QueryValue("context") String context);

    /**
     * Hashes a raw string securely using the Hash-Service.
     *
     * @param rawData The raw sensitive string.
     * @return A Mono emitting the hashed string.
     */
    @Get("/hash")
    Mono<String> hashData(@QueryValue("raw") String rawData);
}