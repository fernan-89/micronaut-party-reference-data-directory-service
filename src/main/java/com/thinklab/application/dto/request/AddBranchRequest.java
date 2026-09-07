package com.thinklab.application.dto.request;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for adding a new branch to an existing company.
 */
@Serdeable
public record AddBranchRequest(
        @NotBlank(message = "Branch Name is required")
        String branchName,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Country is required")
        String country,

        @NotBlank(message = "Zip Code is required")
        String zipCode
) {}
