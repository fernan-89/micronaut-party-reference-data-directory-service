package com.thinklab.application.mapper;

import com.thinklab.application.dto.request.CreateCompanyRequest;
import com.thinklab.application.dto.response.CompanyResponse;
import com.thinklab.domain.model.Company;
import com.thinklab.domain.model.Company.Billing;

import java.util.UUID;

/**
 * Static factory mapper for Company DTOs and Domain Entities.
 * Enforces strict DTO Isolation Pattern.
 */
public final class CompanyMapper {

    private CompanyMapper() {
        // Prevents instantiation of utility class
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Converts a valid request and a sovereign UUID into a pure Domain Aggregate.
     *
     * @param request The validated DTO payload.
     * @param sovereignId The cryptographically generated UUID v4.
     * @return A pure Company Domain Model.
     */
    public static Company toDomain(CreateCompanyRequest request, UUID sovereignId) {
        Billing domainBilling = new Billing(
                request.billing().billingEmail(),
                request.billing().currency(),
                request.billing().taxRegime()
        );

        return Company.createNew(
                sovereignId,
                request.corporateName(),
                request.tradeName(),
                request.taxIdentifier(),
                domainBilling
        );
    }

    /**
     * Converts a pure Domain Aggregate into an Output DTO.
     *
     * @param company The domain aggregate.
     * @return The response payload preventing domain leakage.
     */
    public static CompanyResponse toResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getCorporateName(),
                company.getTradeName(),
                company.getTaxIdentifier(),
                company.getStatus().name(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}