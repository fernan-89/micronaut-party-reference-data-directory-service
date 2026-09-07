package com.thinklab.application.usecase;

import com.thinklab.application.dto.request.AddContactRequest;
import com.thinklab.application.mapper.CompanyMapper;
import com.thinklab.domain.port.HashServicePort;
import com.thinklab.domain.repository.CompanyRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use Case to add a contact to a Company aggregate.
 */
@Singleton
public class AddContactUseCase {

    private static final Logger log = LoggerFactory.getLogger(AddContactUseCase.class);

    private final HashServicePort hashServicePort;
    private final CompanyRepository companyRepository;

    public AddContactUseCase(HashServicePort hashServicePort, CompanyRepository companyRepository) {
        this.hashServicePort = hashServicePort;
        this.companyRepository = companyRepository;
    }

    public Mono<Void> execute(UUID companyId, AddContactRequest request) {
        log.info("[USE CASE] Adding contact '{}' to company ID: {}", request.fullName(), companyId);

        return hashServicePort.generateSovereignId("contact-creation")
                .map(contactId -> CompanyMapper.toContact(request, contactId))
                .flatMap(contact -> companyRepository.addContact(companyId, contact));
    }
}
