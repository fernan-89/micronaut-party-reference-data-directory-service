package com.thinklab.application.usecase;

import com.thinklab.application.dto.request.UpdateOrganisationBillingRequest;
import com.thinklab.application.mapper.OrganisationMapper;
import com.thinklab.domain.repository.OrganisationRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use Case to update Organisation billing information (BIAN Behavior Qualifier: {@code billing/update}).
 */
@Singleton
public class UpdateOrganisationBillingUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateOrganisationBillingUseCase.class);

    private final OrganisationRepository organisationRepository;

    public UpdateOrganisationBillingUseCase(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    public Mono<Void> execute(UUID id, UpdateOrganisationBillingRequest request) {
        log.info("[USE CASE] Updating billing info for ID: {}", id);

        return organisationRepository.updateBilling(id, OrganisationMapper.toBilling(request));
    }
}
