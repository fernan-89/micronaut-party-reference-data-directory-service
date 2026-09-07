package com.thinklab.application.usecase;

import com.thinklab.application.dto.request.UpdateBillingRequest;
import com.thinklab.application.mapper.CompanyMapper;
import com.thinklab.domain.repository.CompanyRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use Case to update company billing information.
 */
@Singleton
public class UpdateBillingUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateBillingUseCase.class);

    private final CompanyRepository companyRepository;

    public UpdateBillingUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Mono<Void> execute(UUID id, UpdateBillingRequest request) {
        log.info("[USE CASE] Updating billing info for ID: {}", id);

        return companyRepository.updateBilling(id, CompanyMapper.toBilling(request));
    }
}
