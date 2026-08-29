package com.thinklab.application.usecase;

import com.thinklab.application.dto.request.CreateCompanyRequest;
import com.thinklab.application.dto.response.CompanyResponse;
import com.thinklab.application.mapper.CompanyMapper;
import com.thinklab.domain.repository.CompanyRepository;
import com.thinklab.domain.port.HashServicePort;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Orchestrates the business flow for Company creation.
 * Resides in the Application Layer, bridging inbound requests to Domain and Infrastructure logic.
 */
@Singleton
public class CreateCompanyUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateCompanyUseCase.class);

    private final HashServicePort hashServicePort;
    private final CompanyRepository companyRepository;

    public CreateCompanyUseCase(HashServicePort hashServicePort, CompanyRepository companyRepository) {
        this.hashServicePort = hashServicePort;
        this.companyRepository = companyRepository;
    }

    /**
     * Executes the company creation flow reactively.
     *
     * @param request The validated inbound DTO.
     * @return A Mono emitting the response DTO.
     */
    public Mono<CompanyResponse> execute(CreateCompanyRequest request) {
        log.info("[USE CASE] Initiating company creation for tax identifier: {}", request.taxIdentifier());

        return hashServicePort.generateSovereignId("company-creation")
                .map(sovereignId -> {
                    log.debug("[USE CASE] Sovereign ID generated: {}. Mapping to Domain...", sovereignId);
                    return CompanyMapper.toDomain(request, sovereignId);
                })
                .flatMap(company -> {
                    log.debug("[USE CASE] Persisting the new Aggregate...");
                    // Monolithic save is permitted here as it is the creation of the aggregate
                    return companyRepository.create(company);
                })
                .map(savedCompany -> {
                    log.info("[USE CASE] Company successfully created with ID: {}", savedCompany.getId());
                    return CompanyMapper.toResponse(savedCompany);
                });
    }
}