package com.thinklab.application.usecase;

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

        return organisationRepository.updateOrganisationUnitStatus(organisationId, unitId, targetStatus);
    }
}
