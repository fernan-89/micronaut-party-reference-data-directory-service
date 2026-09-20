package com.thinklab.application.dto.request;

import com.thinklab.domain.model.Organisation.ContactRole;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for adding a new contact to an existing Organisation
 * (BIAN Behavior Qualifier: {@code contact/initiate}).
 */
@Serdeable
public record CaptureOrganisationContactRequest(
        @NotBlank(message = "Full Name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Phone number is required")
        String phoneNumber,

        @NotNull(message = "Role is required")
        ContactRole role
) {}
