package com.thinklab.application.usecase;

import com.thinklab.application.dto.request.UpdateOrganisationRequest;
import com.thinklab.domain.repository.OrganisationRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use Case to update basic Organisation information (BIAN Behavior Qualifier: {@code update}).
 */
@Singleton
public class UpdateOrganisationUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateOrganisationUseCase.class);

    private final OrganisationRepository organisationRepository;

    public UpdateOrganisationUseCase(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    public Mono<Void> execute(UUID id, UpdateOrganisationRequest request) {
        log.info("[USE CASE] Updating organisation basic info for ID: {}", id);

        return organisationRepository.updateBasicInfo(
                id,
                request.corporateName(),
                request.tradeName(),
                request.taxIdentifier()
        );
    }
}
