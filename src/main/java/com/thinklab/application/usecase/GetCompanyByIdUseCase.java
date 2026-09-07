package com.thinklab.application.usecase;

import com.thinklab.application.dto.response.CompanyResponse;
import com.thinklab.application.mapper.CompanyMapper;
import com.thinklab.domain.repository.CompanyRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Use Case to retrieve a Company by its Sovereign Identity.
 */
@Singleton
public class GetCompanyByIdUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetCompanyByIdUseCase.class);

    private final CompanyRepository companyRepository;

    public GetCompanyByIdUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Mono<CompanyResponse> execute(UUID id) {
        log.info("[USE CASE] Fetching company with ID: {}", id);

        return companyRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Company not found with ID: " + id)))
                .map(CompanyMapper::toResponse);
    }
}
