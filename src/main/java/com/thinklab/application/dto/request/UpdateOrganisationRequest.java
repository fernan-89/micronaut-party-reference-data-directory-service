package com.thinklab.application.dto.request;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for updating basic Organisation info (BIAN Behavior Qualifier: {@code update}).
 */
@Serdeable
public record UpdateOrganisationRequest(
        @NotBlank(message = "Corporate Name is required")
        String corporateName,

        @NotBlank(message = "Trade Name is required")
        String tradeName,

        @NotBlank(message = "Tax Identifier is required")
        @Pattern(regexp = "^\\d{14}$", message = "Tax Identifier must contain exactly 14 digits")
        String taxIdentifier
) {}
