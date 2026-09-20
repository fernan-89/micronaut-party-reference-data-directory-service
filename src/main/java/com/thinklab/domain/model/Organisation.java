package com.thinklab.domain.model;

import com.thinklab.domain.exception.InvalidOrganisationStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Core Domain Model representing the Organisation Aggregate Root.
 *
 * <p><b>BIAN Alignment (ADR-014):</b> This is the Control Record of the
 * {@code party-reference-data-directory} Service Domain — the corporate-scoped Party managed by the
 * platform. {@link OrganisationUnit} (formerly {@code Branch}) is a subordinate Behavior Qualifier
 * Instance Record: it stays inside this aggregate's consistency boundary but is individually
 * addressable and controllable via the API.
 *
 * <p>Strictly pure Java. Agnostic of frameworks, databases, or web layers.
 */
public class Organisation {

    private final UUID id;
    private String corporateName;
    private String tradeName;
    private String taxIdentifier;
    private OrganisationStatus status;
    private Billing billing;
    private final List<OrganisationUnit> organisationUnits;
    private final List<Contact> contacts;
    private final Instant createdAt;
    private Instant updatedAt;

    private Organisation(UUID id, String corporateName, String tradeName, String taxIdentifier, Billing billing) {
        this.id = id;
        this.corporateName = corporateName;
        this.tradeName = tradeName;
        this.taxIdentifier = taxIdentifier;
        this.status = OrganisationStatus.PENDING_ACTIVATION;
        this.billing = billing;
        this.organisationUnits = new ArrayList<>();
        this.contacts = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    private Organisation(
            UUID id,
            String corporateName,
            String tradeName,
            String taxIdentifier,
            OrganisationStatus status,
            Billing billing,
            List<OrganisationUnit> organisationUnits,
            List<Contact> contacts,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.corporateName = corporateName;
        this.tradeName = tradeName;
        this.taxIdentifier = taxIdentifier;
        this.status = status != null ? status : OrganisationStatus.PENDING_ACTIVATION;
        this.billing = billing;
        this.organisationUnits = organisationUnits != null ? new ArrayList<>(organisationUnits) : new ArrayList<>();
        this.contacts = contacts != null ? new ArrayList<>(contacts) : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    /**
     * Static factory method for aggregate creation (BIAN Behavior Qualifier: {@code initiate}).
     * The UUID must be provided by the orchestration layer after calling the Hash-Service.
     */
    public static Organisation createNew(UUID id, String corporateName, String tradeName, String taxIdentifier, Billing billing) {
        if (id == null || corporateName == null || taxIdentifier == null) {
            throw new IllegalArgumentException("ID, Corporate Name, and Tax Identifier are mandatory for Organisation creation.");
        }
        return new Organisation(id, corporateName, tradeName, taxIdentifier, billing);
    }

    /**
     * Reconstitutes an existing Organisation aggregate from persistence layer.
     */
    public static Organisation reconstitute(
            UUID id,
            String corporateName,
            String tradeName,
            String taxIdentifier,
            OrganisationStatus status,
            Billing billing,
            List<OrganisationUnit> organisationUnits,
            List<Contact> contacts,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null || corporateName == null || taxIdentifier == null) {
            throw new IllegalArgumentException("ID, Corporate Name, and Tax Identifier are mandatory to reconstitute an Organisation.");
        }
        return new Organisation(id, corporateName, tradeName, taxIdentifier, status, billing, organisationUnits, contacts, createdAt, updatedAt);
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

    /**
     * Behavior Qualifier: {@code control}. Transitions the Organisation to the given target status,
     * enforcing the {@link OrganisationStatus} state machine.
     */
    public void changeStatus(OrganisationStatus newStatus) {
        Objects.requireNonNull(newStatus, "Status cannot be null.");
        this.status.validateTransitionTo(newStatus);
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        changeStatus(OrganisationStatus.ACTIVE);
    }

    public void suspend() {
        changeStatus(OrganisationStatus.SUSPENDED);
    }

    /**
     * Terminal transition (BIAN Behavior Qualifier: {@code control/cancel}). Replaces the previous
     * physical DELETE — the Organisation is never removed from the system of record, only closed.
     */
    public void cancel() {
        changeStatus(OrganisationStatus.CANCELED);
    }

    public void addContact(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null.");
        }
        this.contacts.add(contact);
        this.updatedAt = Instant.now();
    }

    public void addOrganisationUnit(OrganisationUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("OrganisationUnit cannot be null.");
        }
        this.organisationUnits.add(unit);
        this.updatedAt = Instant.now();
    }

    /**
     * Behavior Qualifier: {@code control/suspend} on a subordinate OrganisationUnit.
     */
    public void suspendUnit(UUID unitId) {
        transitionUnit(unitId, OrganisationUnit.OrganisationUnitStatus.SUSPENDED);
    }

    /**
     * Behavior Qualifier: {@code control/reactivate} on a subordinate OrganisationUnit.
     */
    public void reactivateUnit(UUID unitId) {
        transitionUnit(unitId, OrganisationUnit.OrganisationUnitStatus.ACTIVE);
    }

    private void transitionUnit(UUID unitId, OrganisationUnit.OrganisationUnitStatus targetStatus) {
        Objects.requireNonNull(unitId, "OrganisationUnit ID cannot be null.");
        int index = -1;
        for (int i = 0; i < organisationUnits.size(); i++) {
            if (organisationUnits.get(i).unitId().equals(unitId)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new IllegalArgumentException("OrganisationUnit not found within aggregate: " + unitId);
        }
        OrganisationUnit current = organisationUnits.get(index);
        if (current.status() == targetStatus) {
            throw new InvalidOrganisationStatusException(
                    String.format("Idempotency Violation: OrganisationUnit [%s] is already in the [%s] state.", unitId, targetStatus));
        }
        organisationUnits.set(index, current.withStatus(targetStatus));
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
    public OrganisationStatus getStatus() { return status; }
    public Billing getBilling() { return billing; }
    public List<OrganisationUnit> getOrganisationUnits() { return Collections.unmodifiableList(organisationUnits); }
    public List<Contact> getContacts() { return Collections.unmodifiableList(contacts); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // --- Nested Value Objects & Entities (Java 21 Records) ---

    /**
     * Formal lifecycle state machine for the Organisation Control Record, mirroring the
     * {@code HashStatus} pattern from the Hash Token Registry Service Domain (ADR-013).
     */
    public enum OrganisationStatus {
        PENDING_ACTIVATION, ACTIVE, SUSPENDED, CANCELED;

        /**
         * Validates if the transition from the current state to the target state is legally permitted.
         *
         * @throws InvalidOrganisationStatusException if the transition violates business compliance rules
         *                                             or is unnecessarily idempotent.
         */
        public void validateTransitionTo(OrganisationStatus targetStatus) {
            Objects.requireNonNull(targetStatus, "Target OrganisationStatus must not be null for transition validation.");

            if (this == targetStatus) {
                throw new InvalidOrganisationStatusException(String.format(
                        "Idempotency Violation: The Organisation is already in the [%s] state.", this));
            }
            if (!canTransitionTo(targetStatus)) {
                throw new InvalidOrganisationStatusException(String.format(
                        "Compliance Violation: Illegal state transition from [%s] to [%s].", this, targetStatus));
            }
        }

        public boolean canTransitionTo(OrganisationStatus targetStatus) {
            if (targetStatus == null) {
                return false;
            }
            return switch (this) {
                case PENDING_ACTIVATION -> targetStatus == ACTIVE || targetStatus == CANCELED;
                case ACTIVE -> targetStatus == SUSPENDED || targetStatus == CANCELED;
                case SUSPENDED -> targetStatus == ACTIVE || targetStatus == CANCELED;
                case CANCELED -> false;
            };
        }
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

    /**
     * Subordinate Behavior Qualifier Instance Record (formerly {@code Branch}). Individually
     * addressable and controllable, with its own {@link OrganisationUnitStatus} lifecycle,
     * while remaining inside the {@link Organisation} aggregate's consistency boundary.
     */
    public record OrganisationUnit(
            UUID unitId,
            String unitName,
            String address,
            String city,
            String country,
            String zipCode,
            OrganisationUnitStatus status
    ) {
        public OrganisationUnit {
            if (status == null) {
                status = OrganisationUnitStatus.ACTIVE;
            }
        }

        public OrganisationUnit withStatus(OrganisationUnitStatus newStatus) {
            return new OrganisationUnit(unitId, unitName, address, city, country, zipCode, newStatus);
        }

        public enum OrganisationUnitStatus {
            ACTIVE, SUSPENDED
        }
    }
}
