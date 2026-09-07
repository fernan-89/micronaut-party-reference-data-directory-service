package com.thinklab.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Core Domain Model representing the Company Aggregate Root.
 * Strictly pure Java. Agnostic of frameworks, databases, or web layers.
 */
public class Company {

    private final UUID id;
    private String corporateName;
    private String tradeName;
    private String taxIdentifier;
    private CompanyStatus status;
    private Billing billing;
    private final List<Branch> branches;
    private final List<Contact> contacts;
    private final Instant createdAt;
    private Instant updatedAt;

    private Company(UUID id, String corporateName, String tradeName, String taxIdentifier, Billing billing) {
        this.id = id;
        this.corporateName = corporateName;
        this.tradeName = tradeName;
        this.taxIdentifier = taxIdentifier;
        this.status = CompanyStatus.PENDING_ACTIVATION;
        this.billing = billing;
        this.branches = new ArrayList<>();
        this.contacts = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    private Company(
            UUID id,
            String corporateName,
            String tradeName,
            String taxIdentifier,
            CompanyStatus status,
            Billing billing,
            List<Branch> branches,
            List<Contact> contacts,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.corporateName = corporateName;
        this.tradeName = tradeName;
        this.taxIdentifier = taxIdentifier;
        this.status = status != null ? status : CompanyStatus.PENDING_ACTIVATION;
        this.billing = billing;
        this.branches = branches != null ? new ArrayList<>(branches) : new ArrayList<>();
        this.contacts = contacts != null ? new ArrayList<>(contacts) : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    /**
     * Static factory method for aggregate creation.
     * The UUID must be provided by the orchestration layer after calling the Hash-Service.
     */
    public static Company createNew(UUID id, String corporateName, String tradeName, String taxIdentifier, Billing billing) {
        if (id == null || corporateName == null || taxIdentifier == null) {
            throw new IllegalArgumentException("ID, Corporate Name, and Tax Identifier are mandatory for Company creation.");
        }
        return new Company(id, corporateName, tradeName, taxIdentifier, billing);
    }

    /**
     * Reconstitutes an existing Company aggregate from persistence layer.
     */
    public static Company reconstitute(
            UUID id,
            String corporateName,
            String tradeName,
            String taxIdentifier,
            CompanyStatus status,
            Billing billing,
            List<Branch> branches,
            List<Contact> contacts,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null || corporateName == null || taxIdentifier == null) {
            throw new IllegalArgumentException("ID, Corporate Name, and Tax Identifier are mandatory to reconstitute a Company.");
        }
        return new Company(id, corporateName, tradeName, taxIdentifier, status, billing, branches, contacts, createdAt, updatedAt);
    }

    // --- Domain Behaviors (State Mutations) ---

    public void updateBasicInfo(String corporateName, String tradeName, String taxIdentifier) {
        if (corporateName == null || corporateName.isBlank() || taxIdentifier == null || taxIdentifier.isBlank()) {
            throw new IllegalArgumentException("Corporate Name and Tax Identifier cannot be empty.");
        }
        this.corporateName = corporateName;
        this.tradeName = tradeName;
        this.taxIdentifier = taxIdentifier;
        this.updatedAt = Instant.now();
    }

    public void changeStatus(CompanyStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }
        if (this.status == CompanyStatus.SUSPENDED && newStatus == CompanyStatus.ACTIVE) {
            throw new IllegalStateException("Cannot activate a suspended company.");
        }
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        if (this.status == CompanyStatus.SUSPENDED) {
            throw new IllegalStateException("Cannot activate a suspended company.");
        }
        this.status = CompanyStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void suspend() {
        this.status = CompanyStatus.SUSPENDED;
        this.updatedAt = Instant.now();
    }

    public void addContact(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null.");
        }
        this.contacts.add(contact);
        this.updatedAt = Instant.now();
    }

    public void addBranch(Branch branch) {
        if (branch == null) {
            throw new IllegalArgumentException("Branch cannot be null.");
        }
        this.branches.add(branch);
        this.updatedAt = Instant.now();
    }

    public void updateBilling(Billing newBilling) {
        if (newBilling == null) {
            throw new IllegalArgumentException("Billing information cannot be null.");
        }
        this.billing = newBilling;
        this.updatedAt = Instant.now();
    }

    // --- Getters (Returning Unmodifiable Collections for Encapsulation) ---

    public UUID getId() { return id; }
    public String getCorporateName() { return corporateName; }
    public String getTradeName() { return tradeName; }
    public String getTaxIdentifier() { return taxIdentifier; }
    public CompanyStatus getStatus() { return status; }
    public Billing getBilling() { return billing; }
    public List<Branch> getBranches() { return Collections.unmodifiableList(branches); }
    public List<Contact> getContacts() { return Collections.unmodifiableList(contacts); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // --- Nested Value Objects & Entities (Java 21 Records) ---

    public enum CompanyStatus {
        PENDING_ACTIVATION, ACTIVE, SUSPENDED, CANCELED
    }

    public record Billing(
            String billingEmail,
            String currency,
            String taxRegime
    ) {}

    public record Contact(
            UUID contactId,
            String fullName,
            String email,
            String phoneNumber,
            ContactRole role
    ) {}

    public enum ContactRole {
        ADMIN, BILLING, TECHNICAL, OPERATIONS
    }

    public record Branch(
            UUID branchId,
            String branchName,
            String address,
            String city,
            String country,
            String zipCode
    ) {}
}
