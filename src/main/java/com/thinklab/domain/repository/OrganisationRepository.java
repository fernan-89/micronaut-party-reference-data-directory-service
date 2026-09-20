package com.thinklab.domain.repository;

import com.thinklab.domain.model.Organisation;
import com.thinklab.domain.model.Organisation.Billing;
import com.thinklab.domain.model.Organisation.Contact;
import com.thinklab.domain.model.Organisation.OrganisationStatus;
import com.thinklab.domain.model.Organisation.OrganisationUnit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Outbound Port for Organisation persistence operations (Party Reference Data Directory).
 * Part of the pure Domain Layer.
 *
 * ARCHITECTURAL RULE: Partial State Mutations.
 * Monolithic save operations are strictly reserved for aggregate creation.
 * All state transitions must be handled via specific, granular update methods
 * to prevent race conditions and optimize I/O operations at the database level.
 *
 * <p>There is no {@code deleteById} — the terminal {@code control/cancel} Behavior Qualifier
 * transitions the Organisation to {@link OrganisationStatus#CANCELED} via {@link #updateStatus},
 * never a physical deletion (ADR-014).
 */
public interface OrganisationRepository {

    Mono<Organisation> create(Organisation organisation);

    Mono<Organisation> findById(UUID id);

    Flux<Organisation> findAll();

    Mono<Void> updateBasicInfo(UUID id, String corporateName, String tradeName, String taxIdentifier);

    Mono<Void> updateStatus(UUID id, OrganisationStatus status);

    Mono<Void> addOrganisationUnit(UUID id, OrganisationUnit unit);

    /**
     * Partial Mutation: Updates the operational status of a specific subordinate OrganisationUnit.
     *
     * @param id     The UUID v4 of the parent Organisation.
     * @param unitId The UUID v4 of the target OrganisationUnit.
     * @param status The new status to apply to the unit.
     * @return A Mono signaling completion.
     */
    Mono<Void> updateOrganisationUnitStatus(UUID id, UUID unitId, OrganisationUnit.OrganisationUnitStatus status);

    Mono<Void> addContact(UUID id, Contact contact);

    Mono<Void> updateBilling(UUID id, Billing billing);
}
