package com.thinklab.infrastructure.adapter.in.web;

import com.thinklab.application.dto.request.AddBranchRequest;
import com.thinklab.application.dto.request.AddContactRequest;
import com.thinklab.application.dto.request.CreateCompanyRequest;
import com.thinklab.application.dto.request.UpdateBillingRequest;
import com.thinklab.application.dto.request.UpdateCompanyRequest;
import com.thinklab.application.dto.request.UpdateCompanyStatusRequest;
import com.thinklab.application.dto.response.CompanyResponse;
import com.thinklab.application.usecase.AddBranchUseCase;
import com.thinklab.application.usecase.AddContactUseCase;
import com.thinklab.application.usecase.CreateCompanyUseCase;
import com.thinklab.application.usecase.DeleteCompanyUseCase;
import com.thinklab.application.usecase.GetCompanyByIdUseCase;
import com.thinklab.application.usecase.ListCompaniesUseCase;
import com.thinklab.application.usecase.UpdateBillingUseCase;
import com.thinklab.application.usecase.UpdateCompanyStatusUseCase;
import com.thinklab.application.usecase.UpdateCompanyUseCase;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.Status;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Inbound Web Adapter for Company operations.
 * Exposes REST endpoints and delegates business orchestration to the Application Layer.
 */
@Controller("/api/v1/companies")
public class CompanyController {

    private static final Logger log = LoggerFactory.getLogger(CompanyController.class);

    private final CreateCompanyUseCase createCompanyUseCase;
    private final GetCompanyByIdUseCase getCompanyByIdUseCase;
    private final ListCompaniesUseCase listCompaniesUseCase;
    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final UpdateCompanyStatusUseCase updateCompanyStatusUseCase;
    private final UpdateBillingUseCase updateBillingUseCase;
    private final AddBranchUseCase addBranchUseCase;
    private final AddContactUseCase addContactUseCase;
    private final DeleteCompanyUseCase deleteCompanyUseCase;

    public CompanyController(
            CreateCompanyUseCase createCompanyUseCase,
            GetCompanyByIdUseCase getCompanyByIdUseCase,
            ListCompaniesUseCase listCompaniesUseCase,
            UpdateCompanyUseCase updateCompanyUseCase,
            UpdateCompanyStatusUseCase updateCompanyStatusUseCase,
            UpdateBillingUseCase updateBillingUseCase,
            AddBranchUseCase addBranchUseCase,
            AddContactUseCase addContactUseCase,
            DeleteCompanyUseCase deleteCompanyUseCase
    ) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
        this.listCompaniesUseCase = listCompaniesUseCase;
        this.updateCompanyUseCase = updateCompanyUseCase;
        this.updateCompanyStatusUseCase = updateCompanyStatusUseCase;
        this.updateBillingUseCase = updateBillingUseCase;
        this.addBranchUseCase = addBranchUseCase;
        this.addContactUseCase = addContactUseCase;
        this.deleteCompanyUseCase = deleteCompanyUseCase;
    }

    /**
     * Endpoint to create a new Company Aggregate.
     */
    @Post
    public Mono<HttpResponse<CompanyResponse>> createCompany(@Body @Valid CreateCompanyRequest request) {
        log.info("[WEB ADAPTER] Received request to create company with tax identifier: {}", request.taxIdentifier());

        return createCompanyUseCase.execute(request)
                .map(HttpResponse::created);
    }

    /**
     * Endpoint to retrieve a Company by its Sovereign Identity.
     */
    @Get("/{id}")
    public Mono<HttpResponse<CompanyResponse>> getCompanyById(@PathVariable UUID id) {
        log.info("[WEB ADAPTER] Received request to get company by ID: {}", id);

        return getCompanyByIdUseCase.execute(id)
                .map(HttpResponse::ok);
    }

    /**
     * Endpoint to list all Companies.
     */
    @Get
    public Flux<CompanyResponse> listCompanies() {
        log.info("[WEB ADAPTER] Received request to list all companies");

        return listCompaniesUseCase.execute();
    }

    /**
     * Endpoint to update basic info of a Company.
     */
    @Put("/{id}")
    public Mono<HttpResponse<Void>> updateCompany(@PathVariable UUID id, @Body @Valid UpdateCompanyRequest request) {
        log.info("[WEB ADAPTER] Received request to update company info for ID: {}", id);

        return updateCompanyUseCase.execute(id, request)
                .thenReturn(HttpResponse.noContent());
    }

    /**
     * Endpoint to update operational status of a Company.
     */
    @Patch("/{id}/status")
    public Mono<HttpResponse<Void>> updateStatus(@PathVariable UUID id, @Body @Valid UpdateCompanyStatusRequest request) {
        log.info("[WEB ADAPTER] Received request to update company status for ID: {}", id);

        return updateCompanyStatusUseCase.execute(id, request)
                .thenReturn(HttpResponse.noContent());
    }

    /**
     * Endpoint to update billing info of a Company.
     */
    @Put("/{id}/billing")
    public Mono<HttpResponse<Void>> updateBilling(@PathVariable UUID id, @Body @Valid UpdateBillingRequest request) {
        log.info("[WEB ADAPTER] Received request to update billing for ID: {}", id);

        return updateBillingUseCase.execute(id, request)
                .thenReturn(HttpResponse.noContent());
    }

    /**
     * Endpoint to add a branch to a Company.
     */
    @Post("/{id}/branches")
    @Status(HttpStatus.CREATED)
    public Mono<HttpResponse<Void>> addBranch(@PathVariable UUID id, @Body @Valid AddBranchRequest request) {
        log.info("[WEB ADAPTER] Received request to add branch for ID: {}", id);

        return addBranchUseCase.execute(id, request)
                .thenReturn(HttpResponse.status(HttpStatus.CREATED));
    }

    /**
     * Endpoint to add a contact to a Company.
     */
    @Post("/{id}/contacts")
    @Status(HttpStatus.CREATED)
    public Mono<HttpResponse<Void>> addContact(@PathVariable UUID id, @Body @Valid AddContactRequest request) {
        log.info("[WEB ADAPTER] Received request to add contact for ID: {}", id);

        return addContactUseCase.execute(id, request)
                .thenReturn(HttpResponse.status(HttpStatus.CREATED));
    }

    /**
     * Endpoint to delete a Company.
     */
    @Delete("/{id}")
    public Mono<HttpResponse<Void>> deleteCompany(@PathVariable UUID id) {
        log.info("[WEB ADAPTER] Received request to delete company with ID: {}", id);

        return deleteCompanyUseCase.execute(id)
                .thenReturn(HttpResponse.noContent());
    }
}
