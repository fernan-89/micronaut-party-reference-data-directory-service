package com.thinklab.application.usecase;

import com.thinklab.application.dto.request.CreateOrganisationRequest;
import com.thinklab.application.dto.response.OrganisationResponse;
import com.thinklab.application.mapper.OrganisationMapper;
import com.thinklab.domain.repository.OrganisationRepository;
import com.thinklab.domain.port.HashServicePort;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Orchestrates the business flow for Organisation creation (BIAN Behavior Qualifier: {@code initiate}).
 * Resides in the Application Layer, bridging inbound requests to Domain and Infrastructure logic.
 */
@Singleton
public class InitiateOrganisationUseCase {

    private static final Logger log = LoggerFactory.getLogger(InitiateOrganisationUseCase.class);

    private final HashServicePort hashServicePort;
    private final OrganisationRepository organisationRepository;

    public InitiateOrganisationUseCase(HashServicePort hashServicePort, OrganisationRepository organisationRepository) {
        this.hashServicePort = hashServicePort;
        this.organisationRepository = organisationRepository;
    }

    /**
     * Executes the Organisation initiation flow reactively.
     *
     * @param request The validated inbound DTO.
     * @return A Mono emitting the response DTO.
     */
    public Mono<OrganisationResponse> execute(CreateOrganisationRequest request) {
        log.info("[USE CASE] Initiating organisation creation for tax identifier: {}", request.taxIdentifier());

        return hashServicePort.generateSovereignId("organisation-creation")
                .map(sovereignId -> {
                    log.debug("[USE CASE] Sovereign ID generated: {}. Mapping to Domain...", sovereignId);
                    return OrganisationMapper.toDomain(request, sovereignId);
                })
                .flatMap(organisation -> {
                    log.debug("[USE CASE] Persisting the new Aggregate...");
                    // Monolithic save is permitted here as it is the creation of the aggregate
                    return organisationRepository.create(organisation);
                })
                .map(saved -> {
                    log.info("[USE CASE] Organisation successfully created with ID: {}", saved.getId());
                    return OrganisationMapper.toResponse(saved);
                });
    }
}
