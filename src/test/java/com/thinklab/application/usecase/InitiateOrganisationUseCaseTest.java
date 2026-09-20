package com.thinklab.application.usecase;

import com.thinklab.application.dto.request.CreateOrganisationRequest;
import com.thinklab.domain.model.Organisation;
import com.thinklab.domain.port.HashServicePort;
import com.thinklab.domain.repository.OrganisationRepository;
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
class InitiateOrganisationUseCaseTest {

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private HashServicePort hashServicePort;

    @InjectMocks
    private InitiateOrganisationUseCase initiateOrganisationUseCase;

    @Test
    @DisplayName("Should successfully coordinate HashService and create an Organisation aggregate")
    void testInitiateOrganisationSuccess() {
        UUID generatedId = UUID.randomUUID();
        CreateOrganisationRequest request = new CreateOrganisationRequest(
                "Tech Corp", "Tech", "12345678000100",
                new CreateOrganisationRequest.BillingDto("billing@tech.com", "USD", "SIMPLES")
        );

        when(hashServicePort.generateSovereignId(any())).thenReturn(Mono.just(generatedId));
        when(organisationRepository.create(any(Organisation.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(initiateOrganisationUseCase.execute(request))
                .expectNextMatches(response -> response.id().equals(generatedId)
                        && response.corporateName().equals("Tech Corp")
                        && response.status().equals(Organisation.OrganisationStatus.PENDING_ACTIVATION.name()))
                .verifyComplete();

        verify(hashServicePort, times(1)).generateSovereignId("organisation-creation");
        verify(organisationRepository, times(1)).create(any(Organisation.class));
    }

    @Test
    @DisplayName("Should propagate error when HashService fails")
    void testInitiateOrganisationHashServiceFailure() {
        CreateOrganisationRequest request = new CreateOrganisationRequest(
                "Tech Corp", "Tech", "12345678000100",
                new CreateOrganisationRequest.BillingDto("billing@tech.com", "USD", "SIMPLES")
        );

        when(hashServicePort.generateSovereignId(any())).thenReturn(Mono.error(new IllegalStateException("Hash Token Registry unavailable")));

        StepVerifier.create(initiateOrganisationUseCase.execute(request))
                .expectError(IllegalStateException.class)
                .verify();

        verify(organisationRepository, never()).create(any(Organisation.class));
    }
}
