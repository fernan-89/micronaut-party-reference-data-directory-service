package com.thinklab.application.usecase;

import com.thinklab.domain.repository.CompanyRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use Case to delete a Company aggregate.
 */
@Singleton
public class DeleteCompanyUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteCompanyUseCase.class);

    private final CompanyRepository companyRepository;

    public DeleteCompanyUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Mono<Void> execute(UUID id) {
        log.info("[USE CASE] Deleting company with ID: {}", id);

        return companyRepository.deleteById(id);
    }
}
