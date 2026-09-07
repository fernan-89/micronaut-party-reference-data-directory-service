package com.thinklab.application.usecase;

import com.thinklab.application.dto.request.AddBranchRequest;
import com.thinklab.application.mapper.CompanyMapper;
import com.thinklab.domain.port.HashServicePort;
import com.thinklab.domain.repository.CompanyRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use Case to add a branch to a Company aggregate.
 */
@Singleton
public class AddBranchUseCase {

    private static final Logger log = LoggerFactory.getLogger(AddBranchUseCase.class);

    private final HashServicePort hashServicePort;
    private final CompanyRepository companyRepository;

    public AddBranchUseCase(HashServicePort hashServicePort, CompanyRepository companyRepository) {
        this.hashServicePort = hashServicePort;
        this.companyRepository = companyRepository;
    }

    public Mono<Void> execute(UUID companyId, AddBranchRequest request) {
        log.info("[USE CASE] Adding branch '{}' to company ID: {}", request.branchName(), companyId);

        return hashServicePort.generateSovereignId("branch-creation")
                .map(branchId -> CompanyMapper.toBranch(request, branchId))
                .flatMap(branch -> companyRepository.addBranch(companyId, branch));
    }
}
