package com.thinklab.domain.repository;

import com.thinklab.domain.model.Company;
import com.thinklab.domain.model.Company.Billing;
import com.thinklab.domain.model.Company.Branch;
import com.thinklab.domain.model.Company.CompanyStatus;
import com.thinklab.domain.model.Company.Contact;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Outbound Port for Company persistence operations.
 * Part of the pure Domain Layer.
 *
 * ARCHITECTURAL RULE: Partial State Mutations.
 * Monolithic save operations are strictly reserved for aggregate creation.
 * All state transitions must be handled via specific, granular update methods
 * to prevent race conditions and optimize I/O operations at the database level.
 */
public interface CompanyRepository {

    /**
     * Monolithic save reserved exclusively for the creation of a new Aggregate.
     *
     * @param company The fully constructed Company aggregate.
     * @return A Mono emitting the successfully persisted Company.
     */
    Mono<Company> create(Company company);

    /**
     * Retrieves a Company aggregate by its Sovereign Identity (UUID).
     *
     * @param id The UUID v4 of the company.
     * @return A Mono emitting the Company if found, or empty if not.
     */
    Mono<Company> findById(UUID id);

    /**
     * Partial Mutation: Updates the operational status of the Company.
     * Infrastructure must translate this to a $set operation bounded by the ID.
     *
     * @param id The UUID v4 of the company.
     * @param status The new status to be applied.
     * @return A Mono signaling completion.
     */
    Mono<Void> updateStatus(UUID id, CompanyStatus status);

    /**
     * Partial Mutation: Appends a new Branch to the Company.
     * Infrastructure must translate this to a $push operation bounded by the ID.
     *
     * @param id The UUID v4 of the company.
     * @param branch The new Branch value object.
     * @return A Mono signaling completion.
     */
    Mono<Void> addBranch(UUID id, Branch branch);

    /**
     * Partial Mutation: Appends a new Contact to the Company.
     * Infrastructure must translate this to a $push operation bounded by the ID.
     *
     * @param id The UUID v4 of the company.
     * @param contact The new Contact value object.
     * @return A Mono signaling completion.
     */
    Mono<Void> addContact(UUID id, Contact contact);

    /**
     * Partial Mutation: Updates the billing details of the Company.
     * Infrastructure must translate this to a $set operation bounded by the ID.
     *
     * @param id The UUID v4 of the company.
     * @param billing The new Billing value object.
     * @return A Mono signaling completion.
     */
    Mono<Void> updateBilling(UUID id, Billing billing);
}