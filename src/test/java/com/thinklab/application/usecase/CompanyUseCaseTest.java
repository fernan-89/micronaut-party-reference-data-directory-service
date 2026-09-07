package com.thinklab.application.usecase;

import com.thinklab.application.dto.request.AddBranchRequest;
import com.thinklab.application.dto.request.AddContactRequest;
import com.thinklab.application.dto.request.CreateCompanyRequest;
import com.thinklab.application.dto.request.UpdateBillingRequest;
import com.thinklab.application.dto.request.UpdateCompanyRequest;
import com.thinklab.application.dto.request.UpdateCompanyStatusRequest;
import com.thinklab.application.dto.response.CompanyResponse;
import com.thinklab.domain.model.Company;
import com.thinklab.domain.model.Company.CompanyStatus;
import com.thinklab.domain.model.Company.ContactRole;
import com.thinklab.domain.port.HashServicePort;
import com.thinklab.domain.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyUseCaseTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private HashServicePort hashServicePort;

    private UUID companyId;
    private Company company;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        company = Company.createNew(companyId, "Acme Corp", "Acme", "12345678000199", null);
    }

    @Test
    void testCreateCompanyUseCase() {
        CreateCompanyUseCase useCase = new CreateCompanyUseCase(hashServicePort, companyRepository);
        CreateCompanyRequest request = new CreateCompanyRequest(
                "Acme Corp",
                "Acme",
                "12345678000199",
                new CreateCompanyRequest.BillingDto("billing@acme.com", "USD", "SIMPLES")
        );

        when(hashServicePort.generateSovereignId("company-creation")).thenReturn(Mono.just(companyId));
        when(companyRepository.create(any(Company.class))).thenReturn(Mono.just(company));

        StepVerifier.create(useCase.execute(request))
                .assertNext(res -> {
                    assertEquals(companyId, res.id());
                    assertEquals("Acme Corp", res.corporateName());
                })
                .verifyComplete();
    }

    @Test
    void testGetCompanyByIdUseCaseSuccess() {
        GetCompanyByIdUseCase useCase = new GetCompanyByIdUseCase(companyRepository);
        when(companyRepository.findById(companyId)).thenReturn(Mono.just(company));

        StepVerifier.create(useCase.execute(companyId))
                .assertNext(res -> assertEquals("Acme Corp", res.corporateName()))
                .verifyComplete();
    }

    @Test
    void testGetCompanyByIdUseCaseNotFound() {
        GetCompanyByIdUseCase useCase = new GetCompanyByIdUseCase(companyRepository);
        when(companyRepository.findById(companyId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(companyId))
                .expectError(NoSuchElementException.class)
                .verify();
    }

    @Test
    void testListCompaniesUseCase() {
        ListCompaniesUseCase useCase = new ListCompaniesUseCase(companyRepository);
        when(companyRepository.findAll()).thenReturn(Flux.just(company));

        StepVerifier.create(useCase.execute())
                .assertNext(res -> assertEquals("Acme Corp", res.corporateName()))
                .verifyComplete();
    }

    @Test
    void testUpdateCompanyUseCase() {
        UpdateCompanyUseCase useCase = new UpdateCompanyUseCase(companyRepository);
        UpdateCompanyRequest request = new UpdateCompanyRequest("Acme New", "Trade", "12345678000199");
        when(companyRepository.updateBasicInfo(companyId, "Acme New", "Trade", "12345678000199")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(companyId, request))
                .verifyComplete();
    }

    @Test
    void testUpdateCompanyStatusUseCase() {
        UpdateCompanyStatusUseCase useCase = new UpdateCompanyStatusUseCase(companyRepository);
        UpdateCompanyStatusRequest request = new UpdateCompanyStatusRequest(CompanyStatus.ACTIVE);
        when(companyRepository.updateStatus(companyId, CompanyStatus.ACTIVE)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(companyId, request))
                .verifyComplete();
    }

    @Test
    void testUpdateBillingUseCase() {
        UpdateBillingUseCase useCase = new UpdateBillingUseCase(companyRepository);
        UpdateBillingRequest request = new UpdateBillingRequest("billing@acme.com", "USD", "LUCRO_REAL");
        when(companyRepository.updateBilling(eq(companyId), any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(companyId, request))
                .verifyComplete();
    }

    @Test
    void testAddBranchUseCase() {
        AddBranchUseCase useCase = new AddBranchUseCase(hashServicePort, companyRepository);
        AddBranchRequest request = new AddBranchRequest("Branch 1", "Street", "City", "Country", "12345");
        UUID branchId = UUID.randomUUID();

        when(hashServicePort.generateSovereignId("branch-creation")).thenReturn(Mono.just(branchId));
        when(companyRepository.addBranch(eq(companyId), any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(companyId, request))
                .verifyComplete();
    }

    @Test
    void testAddContactUseCase() {
        AddContactUseCase useCase = new AddContactUseCase(hashServicePort, companyRepository);
        AddContactRequest request = new AddContactRequest("John", "john@acme.com", "1234", ContactRole.ADMIN);
        UUID contactId = UUID.randomUUID();

        when(hashServicePort.generateSovereignId("contact-creation")).thenReturn(Mono.just(contactId));
        when(companyRepository.addContact(eq(companyId), any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(companyId, request))
                .verifyComplete();
    }

    @Test
    void testDeleteCompanyUseCase() {
        DeleteCompanyUseCase useCase = new DeleteCompanyUseCase(companyRepository);
        when(companyRepository.deleteById(companyId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(companyId))
                .verifyComplete();
    }
}
