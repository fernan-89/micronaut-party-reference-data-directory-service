package com.thinklab.application.usecase;

import com.thinklab.application.dto.request.UpdateCompanyStatusRequest;
import com.thinklab.domain.repository.CompanyRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use Case to update company status.
 */
@Singleton
public class UpdateCompanyStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateCompanyStatusUseCase.class);

    private final CompanyRepository companyRepository;

    public UpdateCompanyStatusUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Mono<Void> execute(UUID id, UpdateCompanyStatusRequest request) {
        log.info("[USE CASE] Updating company status to {} for ID: {}", request.status(), id);

        return companyRepository.updateStatus(id, request.status());
    }
}
