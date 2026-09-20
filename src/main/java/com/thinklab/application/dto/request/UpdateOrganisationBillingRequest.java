package com.thinklab.application.dto.request;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for updating Organisation billing info (BIAN Behavior Qualifier: {@code billing/update}).
 */
@Serdeable
public record UpdateOrganisationBillingRequest(
        @NotBlank(message = "Billing Email is required")
        @Email(message = "Invalid billing email format")
        String billingEmail,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code (e.g., USD, BRL)")
        String currency,

        @NotBlank(message = "Tax Regime is required")
        String taxRegime
) {}
