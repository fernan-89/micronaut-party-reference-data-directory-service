package com.thinklab.application.usecase;

import com.thinklab.application.dto.request.CaptureOrganisationContactRequest;
import com.thinklab.application.mapper.OrganisationMapper;
import com.thinklab.domain.port.HashServicePort;
import com.thinklab.domain.repository.OrganisationRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use Case to capture a new contact against an Organisation aggregate
 * (BIAN Behavior Qualifier: {@code contact/initiate}).
 */
@Singleton
public class CaptureOrganisationContactUseCase {

    private static final Logger log = LoggerFactory.getLogger(CaptureOrganisationContactUseCase.class);

    private final HashServicePort hashServicePort;
    private final OrganisationRepository organisationRepository;

    public CaptureOrganisationContactUseCase(HashServicePort hashServicePort, OrganisationRepository organisationRepository) {
        this.hashServicePort = hashServicePort;
        this.organisationRepository = organisationRepository;
    }

    public Mono<Void> execute(UUID organisationId, CaptureOrganisationContactRequest request) {
        log.info("[USE CASE] Adding contact '{}' to organisation ID: {}", request.fullName(), organisationId);

        return hashServicePort.generateSovereignId("contact-creation")
                .map(contactId -> OrganisationMapper.toContact(request, contactId))
                .flatMap(contact -> organisationRepository.addContact(organisationId, contact));
    }
}
