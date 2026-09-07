package com.thinklab.application.dto.response;

import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.List;
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
        BillingResponse billing,
        List<BranchResponse> branches,
        List<ContactResponse> contacts,
        Instant createdAt,
        Instant updatedAt
) {
    @Serdeable
    public record BillingResponse(
            String billingEmail,
            String currency,
            String taxRegime
    ) {}

    @Serdeable
    public record BranchResponse(
            UUID branchId,
            String branchName,
            String address,
            String city,
            String country,
            String zipCode
    ) {}

    @Serdeable
    public record ContactResponse(
            UUID contactId,
            String fullName,
            String email,
            String phoneNumber,
            String role
    ) {}
}
