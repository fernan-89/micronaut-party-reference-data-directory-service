package com.thinklab.application.mapper;

import com.thinklab.application.dto.request.CaptureOrganisationContactRequest;
import com.thinklab.application.dto.request.CreateOrganisationRequest;
import com.thinklab.application.dto.request.InitiateOrganisationUnitRequest;
import com.thinklab.application.dto.request.UpdateOrganisationBillingRequest;
import com.thinklab.application.dto.response.OrganisationResponse;
import com.thinklab.application.dto.response.OrganisationResponse.BillingResponse;
import com.thinklab.application.dto.response.OrganisationResponse.ContactResponse;
import com.thinklab.application.dto.response.OrganisationResponse.OrganisationUnitResponse;
import com.thinklab.domain.model.Organisation;
import com.thinklab.domain.model.Organisation.Billing;
import com.thinklab.domain.model.Organisation.Contact;
import com.thinklab.domain.model.Organisation.OrganisationUnit;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Static factory mapper for Organisation DTOs and Domain Entities.
 * Enforces strict DTO Isolation Pattern.
 */
public final class OrganisationMapper {

    private OrganisationMapper() {
        // Prevents instantiation of utility class
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Converts a valid request and a sovereign UUID into a pure Domain Aggregate.
     *
     * @param request     The validated DTO payload.
     * @param sovereignId The cryptographically generated UUID v4.
     * @return A pure Organisation Domain Model.
     */
    public static Organisation toDomain(CreateOrganisationRequest request, UUID sovereignId) {
        Billing domainBilling = request.billing() != null ? new Billing(
                request.billing().billingEmail(),
                request.billing().currency(),
                request.billing().taxRegime()
        ) : null;

        return Organisation.createNew(
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
     * @param organisation The domain aggregate.
     * @return The response payload preventing domain leakage.
     */
    public static OrganisationResponse toResponse(Organisation organisation) {
        BillingResponse billingResponse = organisation.getBilling() != null ? new BillingResponse(
                organisation.getBilling().billingEmail(),
                organisation.getBilling().currency(),
                organisation.getBilling().taxRegime()
        ) : null;

        List<OrganisationUnitResponse> unitResponses = organisation.getOrganisationUnits() != null ? organisation.getOrganisationUnits().stream()
                .map(u -> new OrganisationUnitResponse(
                        u.unitId(),
                        u.unitName(),
                        u.address(),
                        u.city(),
                        u.country(),
                        u.zipCode(),
                        u.status() != null ? u.status().name() : null
                ))
                .collect(Collectors.toList()) : Collections.emptyList();

        List<ContactResponse> contactResponses = organisation.getContacts() != null ? organisation.getContacts().stream()
                .map(c -> new ContactResponse(
                        c.contactId(),
                        c.fullName(),
                        c.email(),
                        c.phoneNumber(),
                        c.role() != null ? c.role().name() : null
                ))
                .collect(Collectors.toList()) : Collections.emptyList();

        return new OrganisationResponse(
                organisation.getId(),
                organisation.getCorporateName(),
                organisation.getTradeName(),
                organisation.getTaxIdentifier(),
                organisation.getStatus() != null ? organisation.getStatus().name() : null,
                billingResponse,
                unitResponses,
                contactResponses,
                organisation.getCreatedAt(),
                organisation.getUpdatedAt()
        );
    }

    public static Billing toBilling(UpdateOrganisationBillingRequest request) {
        return new Billing(
                request.billingEmail(),
                request.currency(),
                request.taxRegime()
        );
    }

    public static OrganisationUnit toOrganisationUnit(InitiateOrganisationUnitRequest request, UUID unitId) {
        return new OrganisationUnit(
                unitId,
                request.unitName(),
                request.address(),
                request.city(),
                request.country(),
                request.zipCode(),
                OrganisationUnit.OrganisationUnitStatus.ACTIVE
        );
    }

    public static Contact toContact(CaptureOrganisationContactRequest request, UUID contactId) {
        return new Contact(
                contactId,
                request.fullName(),
                request.email(),
                request.phoneNumber(),
                request.role()
        );
    }
}
