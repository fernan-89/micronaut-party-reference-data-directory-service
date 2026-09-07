package com.thinklab.infrastructure.adapter.out.persistence.entity;

import com.thinklab.domain.model.Company;
import com.thinklab.domain.model.Company.Billing;
import com.thinklab.domain.model.Company.Branch;
import com.thinklab.domain.model.Company.CompanyStatus;
import com.thinklab.domain.model.Company.Contact;
import com.thinklab.domain.model.Company.ContactRole;
import io.micronaut.core.annotation.Introspected;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Infrastructure-specific representation of the Company Aggregate for MongoDB.
 * Ensures the pure Domain Model remains untainted by persistence annotations.
 * Uses native BSON annotations for high-performance mapping without ORM overhead.
 */
@Introspected
public class CompanyDocument {

    @BsonId // Native MongoDB driver annotation for Sovereign Identity
    private UUID id;

    private String corporateName;
    private String tradeName;
    private String taxIdentifier;
    private String status;
    private BillingDocument billing;
    private List<BranchDocument> branches = new ArrayList<>();
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
    public List<BranchDocument> getBranches() { return branches; }
    public void setBranches(List<BranchDocument> branches) { this.branches = branches; }
    public List<ContactDocument> getContacts() { return contacts; }
    public void setContacts(List<ContactDocument> contacts) { this.contacts = contacts; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Introspected
    public record BillingDocument(String billingEmail, String currency, String taxRegime) {}

    @Introspected
    public record BranchDocument(UUID branchId, String branchName, String address, String city, String country, String zipCode) {}

    @Introspected
    public record ContactDocument(UUID contactId, String fullName, String email, String phoneNumber, String role) {}

    /**
     * Internal Persistence Mapper ensuring strict isolation between Document and Domain.
     */
    public static final class CompanyPersistenceMapper {

        private CompanyPersistenceMapper() { throw new UnsupportedOperationException(); }

        public static CompanyDocument toDocument(Company company) {
            CompanyDocument doc = new CompanyDocument();
            doc.setId(company.getId());
            doc.setCorporateName(company.getCorporateName());
            doc.setTradeName(company.getTradeName());
            doc.setTaxIdentifier(company.getTaxIdentifier());
            doc.setStatus(company.getStatus().name());
            doc.setCreatedAt(company.getCreatedAt());
            doc.setUpdatedAt(company.getUpdatedAt());

            if (company.getBilling() != null) {
                doc.setBilling(new BillingDocument(company.getBilling().billingEmail(), company.getBilling().currency(), company.getBilling().taxRegime()));
            }

            if (company.getBranches() != null) {
                doc.setBranches(company.getBranches().stream()
                        .map(b -> new BranchDocument(b.branchId(), b.branchName(), b.address(), b.city(), b.country(), b.zipCode()))
                        .collect(Collectors.toList()));
            }

            if (company.getContacts() != null) {
                doc.setContacts(company.getContacts().stream()
                        .map(c -> new ContactDocument(c.contactId(), c.fullName(), c.email(), c.phoneNumber(), c.role().name()))
                        .collect(Collectors.toList()));
            }

            return doc;
        }

        public static Company toDomain(CompanyDocument doc) {
            Billing domainBilling = doc.getBilling() != null ?
                    new Billing(doc.getBilling().billingEmail(), doc.getBilling().currency(), doc.getBilling().taxRegime()) : null;

            List<Branch> domainBranches = doc.getBranches() != null ?
                    doc.getBranches().stream()
                            .map(b -> new Branch(b.branchId(), b.branchName(), b.address(), b.city(), b.country(), b.zipCode()))
                            .collect(Collectors.toList()) : Collections.emptyList();

            List<Contact> domainContacts = doc.getContacts() != null ?
                    doc.getContacts().stream()
                            .map(c -> new Contact(c.contactId(), c.fullName(), c.email(), c.phoneNumber(), ContactRole.valueOf(c.role())))
                            .collect(Collectors.toList()) : Collections.emptyList();

            CompanyStatus status = doc.getStatus() != null ? CompanyStatus.valueOf(doc.getStatus()) : CompanyStatus.PENDING_ACTIVATION;

            return Company.reconstitute(
                    doc.getId(),
                    doc.getCorporateName(),
                    doc.getTradeName(),
                    doc.getTaxIdentifier(),
                    status,
                    domainBilling,
                    domainBranches,
                    domainContacts,
                    doc.getCreatedAt(),
                    doc.getUpdatedAt()
            );
        }
    }
}
