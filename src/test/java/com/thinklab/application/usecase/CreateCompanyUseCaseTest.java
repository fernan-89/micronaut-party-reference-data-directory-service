package com.thinklab.application.usecase;

import com.thinklab.domain.model.Company;
import com.thinklab.domain.port.HashServicePort;
import com.thinklab.domain.repository.CompanyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCompanyUseCaseTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private HashServicePort hashServicePort;

    @InjectMocks
    private CreateCompanyUseCase createCompanyUseCase;

    @Test
    @DisplayName("Should successfully coordinate HashService and create a Company aggregate")
    void testCreateCompanySuccess() {
        UUID generatedId = UUID.randomUUID();
        when(hashServicePort.generateSovereignId(any())).thenReturn(Mono.just(generatedId));
        when(companyRepository.create(any(Company.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(createCompanyUseCase.execute("Tech Corp", "Tech", "12.345.678/0001-00", null))
                .expectNextMatches(company -> company.getId().equals(generatedId)
                        && company.getCorporateName().equals("Tech Corp")
                        && company.getStatus() == Company.CompanyStatus.PENDING_ACTIVATION)
                .verifyComplete();

        verify(hashServicePort, times(1)).generateSovereignId("COMPANY_AGGREGATE_CREATION");
        verify(companyRepository, times(1)).create(any(Company.class));
    }

    @Test
    @DisplayName("Should propagate error when HashService fails")
    void testCreateCompanyHashServiceFailure() {
        when(hashServicePort.generateSovereignId(any())).thenReturn(Mono.error(new IllegalStateException("Hash service unavailable")));

        StepVerifier.create(createCompanyUseCase.execute("Tech Corp", "Tech", "12.345.678/0001-00", null))
                .expectError(IllegalStateException.class)
                .verify();

        verify(companyRepository, never()).create(any(Company.class));
    }
}
