package com.thinklab.application.dto.request;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for Company Creation Request.
 * Acts as a protective barrier to the Domain Layer.
 * Validations run automatically at the framework edge via Jakarta Validation.
 */
@Serdeable
public record CreateCompanyRequest(

        @NotBlank(message = "Corporate Name is required")
        String corporateName,

        @NotBlank(message = "Trade Name is required")
        String tradeName,

        @NotBlank(message = "Tax Identifier is required")
        @Pattern(regexp = "^\\d{14}$", message = "Tax Identifier must contain exactly 14 digits")
        String taxIdentifier,

        @NotNull(message = "Billing information is required")
        @Valid
        BillingDto billing
) {
    /**
     * Nested DTO representing the billing input payload.
     */
    @Serdeable
    public record BillingDto(

            @NotBlank(message = "Billing Email is required")
            @Email(message = "Invalid billing email format")
            String billingEmail,

            @NotBlank(message = "Currency is required")
            @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code (e.g., USD, BRL)")
            String currency,

            @NotBlank(message = "Tax Regime is required")
            String taxRegime
    ) {}
}