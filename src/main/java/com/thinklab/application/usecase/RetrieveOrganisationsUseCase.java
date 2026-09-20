package com.thinklab.application.usecase;

import com.thinklab.application.dto.response.OrganisationResponse;
import com.thinklab.application.mapper.OrganisationMapper;
import com.thinklab.domain.repository.OrganisationRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * Use Case to retrieve all Organisations reactively (BIAN Behavior Qualifier: {@code retrieve}, collection).
 */
@Singleton
public class RetrieveOrganisationsUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetrieveOrganisationsUseCase.class);

    private final OrganisationRepository organisationRepository;

    public RetrieveOrganisationsUseCase(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    public Flux<OrganisationResponse> execute() {
        log.info("[USE CASE] Fetching all organisations");

        return organisationRepository.findAll()
                .map(OrganisationMapper::toResponse);
    }
}
