package com.thinklab.application.dto.request;

import com.thinklab.domain.model.Company.CompanyStatus;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating company status.
 */
@Serdeable
public record UpdateCompanyStatusRequest(
        @NotNull(message = "Status is required")
        CompanyStatus status
) {}
