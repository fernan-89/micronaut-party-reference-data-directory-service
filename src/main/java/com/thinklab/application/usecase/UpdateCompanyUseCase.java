package com.thinklab.application.usecase;

import com.thinklab.application.dto.request.UpdateCompanyRequest;
import com.thinklab.domain.repository.CompanyRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use Case to update basic company information.
 */
@Singleton
public class UpdateCompanyUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateCompanyUseCase.class);

    private final CompanyRepository companyRepository;

    public UpdateCompanyUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Mono<Void> execute(UUID id, UpdateCompanyRequest request) {
        log.info("[USE CASE] Updating company basic info for ID: {}", id);

        return companyRepository.updateBasicInfo(
                id,
                request.corporateName(),
                request.tradeName(),
                request.taxIdentifier()
        );
    }
}
