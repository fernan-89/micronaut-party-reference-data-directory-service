package com.thinklab.application.usecase;

import com.thinklab.application.dto.response.CompanyResponse;
import com.thinklab.application.mapper.CompanyMapper;
import com.thinklab.domain.repository.CompanyRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * Use Case to retrieve all Companies reactively.
 */
@Singleton
public class ListCompaniesUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListCompaniesUseCase.class);

    private final CompanyRepository companyRepository;

    public ListCompaniesUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Flux<CompanyResponse> execute() {
        log.info("[USE CASE] Fetching all companies");

        return companyRepository.findAll()
                .map(CompanyMapper::toResponse);
    }
}
