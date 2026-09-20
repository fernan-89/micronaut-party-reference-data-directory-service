package com.thinklab.application.dto.response;

import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for Organisation Output Payload (Party Reference Data Directory Control Record).
 * Enforces the DTO Isolation Pattern by preventing the pure Domain Model
 * from bleeding out into the HTTP/External boundaries.
 */
@Serdeable
public record OrganisationResponse(
        UUID id,
        String corporateName,
        String tradeName,
        String taxIdentifier,
        String status,
        BillingResponse billing,
        List<OrganisationUnitResponse> organisationUnits,
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
    public record OrganisationUnitResponse(
            UUID unitId,
            String unitName,
            String address,
            String city,
            String country,
            String zipCode,
            String status
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
