package com.thinklab.application.usecase;

import com.thinklab.domain.exception.OrganisationNotFoundException;
import com.thinklab.domain.model.Organisation.OrganisationStatus;
import com.thinklab.domain.repository.OrganisationRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use Case governing the Organisation lifecycle (BIAN Behavior Qualifier: {@code control}).
 *
 * <p>Merges the former {@code UpdateCompanyStatusUseCase} and {@code DeleteCompanyUseCase} into a
 * single Control operation: {@link Action#ACTIVATE}, {@link Action#SUSPEND}, and
 * {@link Action#CANCEL}. {@code CANCEL} replaces the previous physical {@code DELETE} — the
 * Organisation is never removed from the system of record, only closed (ADR-014).
 *
 * <p><b>State Machine Enforcement:</b> The ADR-002 "Partial State Mutations" pattern normally
 * writes directly to MongoDB without loading the aggregate, for I/O efficiency. That alone would
 * bypass {@link OrganisationStatus#validateTransitionTo}, silently allowing illegal transitions
 * (e.g. cancelling an already-CANCELED Organisation). This use case therefore loads the aggregate
 * first, delegates the transition to the domain model (which throws
 * {@link com.thinklab.domain.exception.InvalidOrganisationStatusException} on an illegal move),
 * and only then issues the granular partial update.
 */
@Singleton
public class ControlOrganisationUseCase {

    private static final Logger log = LoggerFactory.getLogger(ControlOrganisationUseCase.class);

    private final OrganisationRepository organisationRepository;

    public ControlOrganisationUseCase(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    public Mono<Void> execute(UUID id, Action action) {
        log.info("[USE CASE] Controlling organisation lifecycle: {} for ID: {}", action, id);

        return organisationRepository.findById(id)
                .switchIfEmpty(Mono.error(new OrganisationNotFoundException(id)))
                .flatMap(organisation -> {
                    organisation.changeStatus(action.targetStatus());
                    return organisationRepository.updateStatus(id, action.targetStatus());
                });
    }

    public enum Action {
        ACTIVATE(OrganisationStatus.ACTIVE),
        SUSPEND(OrganisationStatus.SUSPENDED),
        CANCEL(OrganisationStatus.CANCELED);

        private final OrganisationStatus targetStatus;

        Action(OrganisationStatus targetStatus) {
            this.targetStatus = targetStatus;
        }

        public OrganisationStatus targetStatus() {
            return targetStatus;
        }
    }
}
