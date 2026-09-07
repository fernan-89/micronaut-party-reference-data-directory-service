package com.thinklab.application.mapper;

import com.thinklab.application.dto.request.AddBranchRequest;
import com.thinklab.application.dto.request.AddContactRequest;
import com.thinklab.application.dto.request.CreateCompanyRequest;
import com.thinklab.application.dto.request.UpdateBillingRequest;
import com.thinklab.application.dto.response.CompanyResponse;
import com.thinklab.application.dto.response.CompanyResponse.BillingResponse;
import com.thinklab.application.dto.response.CompanyResponse.BranchResponse;
import com.thinklab.application.dto.response.CompanyResponse.ContactResponse;
import com.thinklab.domain.model.Company;
import com.thinklab.domain.model.Company.Billing;
import com.thinklab.domain.model.Company.Branch;
import com.thinklab.domain.model.Company.Contact;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        Billing domainBilling = request.billing() != null ? new Billing(
                request.billing().billingEmail(),
                request.billing().currency(),
                request.billing().taxRegime()
        ) : null;

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
        BillingResponse billingResponse = company.getBilling() != null ? new BillingResponse(
                company.getBilling().billingEmail(),
                company.getBilling().currency(),
                company.getBilling().taxRegime()
        ) : null;

        List<BranchResponse> branchResponses = company.getBranches() != null ? company.getBranches().stream()
                .map(b -> new BranchResponse(
                        b.branchId(),
                        b.branchName(),
                        b.address(),
                        b.city(),
                        b.country(),
                        b.zipCode()
                ))
                .collect(Collectors.toList()) : Collections.emptyList();

        List<ContactResponse> contactResponses = company.getContacts() != null ? company.getContacts().stream()
                .map(c -> new ContactResponse(
                        c.contactId(),
                        c.fullName(),
                        c.email(),
                        c.phoneNumber(),
                        c.role() != null ? c.role().name() : null
                ))
                .collect(Collectors.toList()) : Collections.emptyList();

        return new CompanyResponse(
                company.getId(),
                company.getCorporateName(),
                company.getTradeName(),
                company.getTaxIdentifier(),
                company.getStatus() != null ? company.getStatus().name() : null,
                billingResponse,
                branchResponses,
                contactResponses,
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }

    public static Billing toBilling(UpdateBillingRequest request) {
        return new Billing(
                request.billingEmail(),
                request.currency(),
                request.taxRegime()
        );
    }

    public static Branch toBranch(AddBranchRequest request, UUID branchId) {
        return new Branch(
                branchId,
                request.branchName(),
                request.address(),
                request.city(),
                request.country(),
                request.zipCode()
        );
    }

    public static Contact toContact(AddContactRequest request, UUID contactId) {
        return new Contact(
                contactId,
                request.fullName(),
                request.email(),
                request.phoneNumber(),
                request.role()
        );
    }
}
