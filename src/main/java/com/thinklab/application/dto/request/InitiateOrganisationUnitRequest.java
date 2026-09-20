package com.thinklab.application.dto.request;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for adding a new OrganisationUnit (formerly Branch) to an existing Organisation
 * (BIAN Behavior Qualifier: {@code organisation-unit/initiate}).
 */
@Serdeable
public record InitiateOrganisationUnitRequest(
        @NotBlank(message = "Unit Name is required")
        String unitName,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Country is required")
        String country,

        @NotBlank(message = "Zip Code is required")
        String zipCode
) {}
