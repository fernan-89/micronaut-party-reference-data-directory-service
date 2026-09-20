package com.thinklab.application.usecase;

import com.thinklab.application.dto.response.OrganisationResponse;
import com.thinklab.application.mapper.OrganisationMapper;
import com.thinklab.domain.exception.OrganisationNotFoundException;
import com.thinklab.domain.repository.OrganisationRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use Case to retrieve an Organisation by its Sovereign Identity (BIAN Behavior Qualifier: {@code retrieve}).
 */
@Singleton
public class RetrieveOrganisationUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetrieveOrganisationUseCase.class);

    private final OrganisationRepository organisationRepository;

    public RetrieveOrganisationUseCase(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    public Mono<OrganisationResponse> execute(UUID id) {
        log.info("[USE CASE] Fetching organisation with ID: {}", id);

        return organisationRepository.findById(id)
                .switchIfEmpty(Mono.error(new OrganisationNotFoundException(id)))
                .map(OrganisationMapper::toResponse);
    }
}
