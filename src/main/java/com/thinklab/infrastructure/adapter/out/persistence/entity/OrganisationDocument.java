package com.thinklab.infrastructure.adapter.out.persistence.entity;

import com.thinklab.domain.model.Organisation;
import com.thinklab.domain.model.Organisation.Billing;
import com.thinklab.domain.model.Organisation.Contact;
import com.thinklab.domain.model.Organisation.ContactRole;
import com.thinklab.domain.model.Organisation.OrganisationStatus;
import com.thinklab.domain.model.Organisation.OrganisationUnit;
import io.micronaut.core.annotation.Introspected;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Infrastructure-specific representation of the Organisation Aggregate for MongoDB.
 * Ensures the pure Domain Model remains untainted by persistence annotations.
 * Uses native BSON annotations for high-performance mapping without ORM overhead.
 */
@Introspected
public class OrganisationDocument {

    @BsonId // Native MongoDB driver annotation for Sovereign Identity
    private UUID id;

    private String corporateName;
    private String tradeName;
    private String taxIdentifier;
    private String status;
    private BillingDocument billing;
    private List<OrganisationUnitDocument> organisationUnits = new ArrayList<>();
    private List<ContactDocument> contacts = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    // Getters and Setters required by framework POJO codec
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCorporateName() { return corporateName; }
    public void setCorporateName(String corporateName) { this.corporateName = corporateName; }
    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public String getTaxIdentifier() { return taxIdentifier; }
    public void setTaxIdentifier(String taxIdentifier) { this.taxIdentifier = taxIdentifier; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BillingDocument getBilling() { return billing; }
    public void setBilling(BillingDocument billing) { this.billing = billing; }
    public List<OrganisationUnitDocument> getOrganisationUnits() { return organisationUnits; }
    public void setOrganisationUnits(List<OrganisationUnitDocument> organisationUnits) { this.organisationUnits = organisationUnits; }
    public List<ContactDocument> getContacts() { return contacts; }
    public void setContacts(List<ContactDocument> contacts) { this.contacts = contacts; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Introspected
    public record BillingDocument(String billingEmail, String currency, String taxRegime) {}

    @Introspected
    public record OrganisationUnitDocument(
            UUID unitId, String unitName, String address, String city, String country, String zipCode, String status
    ) {}

    @Introspected
    public record ContactDocument(UUID contactId, String fullName, String email, String phoneNumber, String role) {}

    /**
     * Internal Persistence Mapper ensuring strict isolation between Document and Domain.
     */
    public static final class OrganisationPersistenceMapper {

        private OrganisationPersistenceMapper() { throw new UnsupportedOperationException(); }

        public static OrganisationDocument toDocument(Organisation organisation) {
            OrganisationDocument doc = new OrganisationDocument();
            doc.setId(organisation.getId());
            doc.setCorporateName(organisation.getCorporateName());
            doc.setTradeName(organisation.getTradeName());
            doc.setTaxIdentifier(organisation.getTaxIdentifier());
            doc.setStatus(organisation.getStatus().name());
            doc.setCreatedAt(organisation.getCreatedAt());
            doc.setUpdatedAt(organisation.getUpdatedAt());

            if (organisation.getBilling() != null) {
                doc.setBilling(new BillingDocument(organisation.getBilling().billingEmail(), organisation.getBilling().currency(), organisation.getBilling().taxRegime()));
            }

            doc.setOrganisationUnits(organisation.getOrganisationUnits().stream()
                    .map(u -> new OrganisationUnitDocument(u.unitId(), u.unitName(), u.address(), u.city(), u.country(), u.zipCode(), u.status().name()))
                    .collect(Collectors.toList()));

            doc.setContacts(organisation.getContacts().stream()
                    .map(c -> new ContactDocument(c.contactId(), c.fullName(), c.email(), c.phoneNumber(), c.role().name()))
                    .collect(Collectors.toList()));

            return doc;
        }

        public static Organisation toDomain(OrganisationDocument doc) {
            Billing domainBilling = doc.getBilling() != null ?
                    new Billing(doc.getBilling().billingEmail(), doc.getBilling().currency(), doc.getBilling().taxRegime()) : null;

            List<OrganisationUnit> domainUnits = doc.getOrganisationUnits() != null ?
                    doc.getOrganisationUnits().stream()
                            .map(u -> new OrganisationUnit(u.unitId(), u.unitName(), u.address(), u.city(), u.country(), u.zipCode(),
                                    u.status() != null ? OrganisationUnit.OrganisationUnitStatus.valueOf(u.status()) : OrganisationUnit.OrganisationUnitStatus.ACTIVE))
                            .collect(Collectors.toList()) : Collections.emptyList();

            List<Contact> domainContacts = doc.getContacts() != null ?
                    doc.getContacts().stream()
                            .map(c -> new Contact(c.contactId(), c.fullName(), c.email(), c.phoneNumber(), ContactRole.valueOf(c.role())))
                            .collect(Collectors.toList()) : Collections.emptyList();

            OrganisationStatus status = doc.getStatus() != null ? OrganisationStatus.valueOf(doc.getStatus()) : OrganisationStatus.PENDING_ACTIVATION;

            return Organisation.reconstitute(
                    doc.getId(),
                    doc.getCorporateName(),
                    doc.getTradeName(),
                    doc.getTaxIdentifier(),
                    status,
                    domainBilling,
                    domainUnits,
                    domainContacts,
                    doc.getCreatedAt(),
                    doc.getUpdatedAt()
            );
        }
    }
}
