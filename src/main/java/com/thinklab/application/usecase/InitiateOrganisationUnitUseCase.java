package com.thinklab.application.usecase;

import com.thinklab.application.dto.request.InitiateOrganisationUnitRequest;
import com.thinklab.application.mapper.OrganisationMapper;
import com.thinklab.domain.port.HashServicePort;
import com.thinklab.domain.repository.OrganisationRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use Case to add a subordinate OrganisationUnit (formerly Branch) to an Organisation aggregate
 * (BIAN Behavior Qualifier: {@code organisation-unit/initiate}).
 */
@Singleton
public class InitiateOrganisationUnitUseCase {

    private static final Logger log = LoggerFactory.getLogger(InitiateOrganisationUnitUseCase.class);

    private final HashServicePort hashServicePort;
    private final OrganisationRepository organisationRepository;

    public InitiateOrganisationUnitUseCase(HashServicePort hashServicePort, OrganisationRepository organisationRepository) {
        this.hashServicePort = hashServicePort;
        this.organisationRepository = organisationRepository;
    }

    public Mono<Void> execute(UUID organisationId, InitiateOrganisationUnitRequest request) {
        log.info("[USE CASE] Adding organisation unit '{}' to organisation ID: {}", request.unitName(), organisationId);

        return hashServicePort.generateSovereignId("organisation-unit-creation")
                .map(unitId -> OrganisationMapper.toOrganisationUnit(request, unitId))
                .flatMap(unit -> organisationRepository.addOrganisationUnit(organisationId, unit));
    }
}
