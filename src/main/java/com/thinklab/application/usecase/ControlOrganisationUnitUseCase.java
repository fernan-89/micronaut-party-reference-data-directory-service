package com.thinklab.application.usecase;

import com.thinklab.domain.exception.OrganisationNotFoundException;
import com.thinklab.domain.model.Organisation;
import com.thinklab.domain.model.Organisation.OrganisationUnit.OrganisationUnitStatus;
import com.thinklab.domain.repository.OrganisationRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use Case governing the lifecycle of a subordinate OrganisationUnit (BIAN Behavior Qualifier:
 * {@code organisation-unit/control/suspend} and {@code organisation-unit/control/reactivate}).
 *
 * <p>Loads the parent aggregate and delegates the transition to
 * {@link Organisation#suspendUnit(UUID)}/{@link Organisation#reactivateUnit(UUID)} before issuing
 * the granular partial update, so the unit's idempotency/state-machine check in the domain model
 * (see {@code Organisation.transitionUnit}) is actually enforced rather than bypassed.
 */
@Singleton
public class ControlOrganisationUnitUseCase {

    private static final Logger log = LoggerFactory.getLogger(ControlOrganisationUnitUseCase.class);

    private final OrganisationRepository organisationRepository;

    public ControlOrganisationUnitUseCase(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    public Mono<Void> execute(UUID organisationId, UUID unitId, OrganisationUnitStatus targetStatus) {
        log.info("[USE CASE] Controlling organisation unit {} lifecycle: {} for organisation ID: {}", unitId, targetStatus, organisationId);

        return organisationRepository.findById(organisationId)
                .switchIfEmpty(Mono.error(new OrganisationNotFoundException(organisationId)))
                .flatMap(organisation -> {
                    if (targetStatus == OrganisationUnitStatus.SUSPENDED) {
                        organisation.suspendUnit(unitId);
                    } else {
                        organisation.reactivateUnit(unitId);
                    }
                    return organisationRepository.updateOrganisationUnitStatus(organisationId, unitId, targetStatus);
                });
    }
}
