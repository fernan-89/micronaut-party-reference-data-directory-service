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
import com.thinklab.domain.model.Company.CompanyStatus;
import com.thinklab.domain.model.Company.ContactRole;
import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyControllerTest {

    @Mock
    private CreateCompanyUseCase createCompanyUseCase;

    @Mock
    private GetCompanyByIdUseCase getCompanyByIdUseCase;

    @Mock
    private ListCompaniesUseCase listCompaniesUseCase;

    @Mock
    private UpdateCompanyUseCase updateCompanyUseCase;

    @Mock
    private UpdateCompanyStatusUseCase updateCompanyStatusUseCase;

    @Mock
    private UpdateBillingUseCase updateBillingUseCase;

    @Mock
    private AddBranchUseCase addBranchUseCase;

    @Mock
    private AddContactUseCase addContactUseCase;

    @Mock
    private DeleteCompanyUseCase deleteCompanyUseCase;

    @InjectMocks
    private CompanyController companyController;

    private UUID companyId;
    private CompanyResponse sampleResponse;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        sampleResponse = new CompanyResponse(
                companyId,
                "Acme Corp",
                "Acme",
                "12345678000199",
                "ACTIVE",
                new CompanyResponse.BillingResponse("billing@acme.com", "USD", "SIMPLES"),
                Collections.emptyList(),
                Collections.emptyList(),
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    void testCreateCompany() {
        CreateCompanyRequest request = new CreateCompanyRequest(
                "Acme Corp",
                "Acme",
                "12345678000199",
                new CreateCompanyRequest.BillingDto("billing@acme.com", "USD", "SIMPLES")
        );

        when(createCompanyUseCase.execute(any(CreateCompanyRequest.class))).thenReturn(Mono.just(sampleResponse));

        StepVerifier.create(companyController.createCompany(request))
                .assertNext(response -> {
                    assertEquals(HttpStatus.CREATED, response.getStatus());
                    assertNotNull(response.body());
                    assertEquals("Acme Corp", response.body().corporateName());
                })
                .verifyComplete();
    }

    @Test
    void testGetCompanyById() {
        when(getCompanyByIdUseCase.execute(companyId)).thenReturn(Mono.just(sampleResponse));

        StepVerifier.create(companyController.getCompanyById(companyId))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatus());
                    assertNotNull(response.body());
                    assertEquals(companyId, response.body().id());
                })
                .verifyComplete();
    }

    @Test
    void testListCompanies() {
        when(listCompaniesUseCase.execute()).thenReturn(Flux.just(sampleResponse));

        StepVerifier.create(companyController.listCompanies())
                .assertNext(res -> assertEquals("Acme Corp", res.corporateName()))
                .verifyComplete();
    }

    @Test
    void testUpdateCompany() {
        UpdateCompanyRequest request = new UpdateCompanyRequest("Acme Corp New", "Acme New", "12345678000199");
        when(updateCompanyUseCase.execute(eq(companyId), any(UpdateCompanyRequest.class))).thenReturn(Mono.empty());

        StepVerifier.create(companyController.updateCompany(companyId, request))
                .assertNext(response -> assertEquals(HttpStatus.NO_CONTENT, response.getStatus()))
                .verifyComplete();
    }

    @Test
    void testUpdateStatus() {
        UpdateCompanyStatusRequest request = new UpdateCompanyStatusRequest(CompanyStatus.ACTIVE);
        when(updateCompanyStatusUseCase.execute(eq(companyId), any(UpdateCompanyStatusRequest.class))).thenReturn(Mono.empty());

        StepVerifier.create(companyController.updateStatus(companyId, request))
                .assertNext(response -> assertEquals(HttpStatus.NO_CONTENT, response.getStatus()))
                .verifyComplete();
    }

    @Test
    void testUpdateBilling() {
        UpdateBillingRequest request = new UpdateBillingRequest("new@acme.com", "BRL", "LUCRO_REAL");
        when(updateBillingUseCase.execute(eq(companyId), any(UpdateBillingRequest.class))).thenReturn(Mono.empty());

        StepVerifier.create(companyController.updateBilling(companyId, request))
                .assertNext(response -> assertEquals(HttpStatus.NO_CONTENT, response.getStatus()))
                .verifyComplete();
    }

    @Test
    void testAddBranch() {
        AddBranchRequest request = new AddBranchRequest("Branch 1", "Main St 100", "City", "Country", "12345");
        when(addBranchUseCase.execute(eq(companyId), any(AddBranchRequest.class))).thenReturn(Mono.empty());

        StepVerifier.create(companyController.addBranch(companyId, request))
                .assertNext(response -> assertEquals(HttpStatus.CREATED, response.getStatus()))
                .verifyComplete();
    }

    @Test
    void testAddContact() {
        AddContactRequest request = new AddContactRequest("John Doe", "john@acme.com", "+123456789", ContactRole.ADMIN);
        when(addContactUseCase.execute(eq(companyId), any(AddContactRequest.class))).thenReturn(Mono.empty());

        StepVerifier.create(companyController.addContact(companyId, request))
                .assertNext(response -> assertEquals(HttpStatus.CREATED, response.getStatus()))
                .verifyComplete();
    }

    @Test
    void testDeleteCompany() {
        when(deleteCompanyUseCase.execute(companyId)).thenReturn(Mono.empty());

        StepVerifier.create(companyController.deleteCompany(companyId))
                .assertNext(response -> assertEquals(HttpStatus.NO_CONTENT, response.getStatus()))
                .verifyComplete();
    }
}
