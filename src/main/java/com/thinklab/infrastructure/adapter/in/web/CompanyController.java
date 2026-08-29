package com.thinklab.infrastructure.adapter.in.web;

import com.thinklab.application.dto.request.CreateCompanyRequest;
import com.thinklab.application.dto.response.CompanyResponse;
import com.thinklab.application.usecase.CreateCompanyUseCase;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Inbound Web Adapter for Company operations.
 * Exposes REST endpoints and delegates business orchestration to the Application Layer.
 */
@Controller("/api/v1/companies")
public class CompanyController {

    private static final Logger log = LoggerFactory.getLogger(CompanyController.class);

    private final CreateCompanyUseCase createCompanyUseCase;

    public CompanyController(CreateCompanyUseCase createCompanyUseCase) {
        this.createCompanyUseCase = createCompanyUseCase;
    }

    /**
     * Endpoint to create a new Company Aggregate.
     * Validations run automatically at the framework edge.
     *
     * @param request The HTTP request body mapped to the DTO.
     * @return A Mono emitting the HTTP 201 Created response.
     */
    @Post
    public Mono<HttpResponse<CompanyResponse>> createCompany(@Body @Valid CreateCompanyRequest request) {
        log.info("[WEB ADAPTER] Received request to create company with tax identifier: {}", request.taxIdentifier());

        return createCompanyUseCase.execute(request)
                .map(HttpResponse::created);
    }
}