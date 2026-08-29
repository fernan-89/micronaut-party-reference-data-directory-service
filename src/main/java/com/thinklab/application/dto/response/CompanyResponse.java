package com.thinklab.application.dto.response;

import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for Company Output Payload.
 * Enforces the DTO Isolation Pattern by preventing the pure Domain Model
 * from bleeding out into the HTTP/External boundaries.
 */
@Serdeable
public record CompanyResponse(
        UUID id,
        String corporateName,
        String tradeName,
        String taxIdentifier,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}