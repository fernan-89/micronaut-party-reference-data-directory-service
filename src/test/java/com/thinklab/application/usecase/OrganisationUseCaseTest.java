package com.thinklab.application.usecase;

import com.thinklab.application.dto.request.CaptureOrganisationContactRequest;
import com.thinklab.application.dto.request.CreateOrganisationRequest;
import com.thinklab.application.dto.request.InitiateOrganisationUnitRequest;
import com.thinklab.application.dto.request.UpdateOrganisationBillingRequest;
import com.thinklab.application.dto.request.UpdateOrganisationRequest;
import com.thinklab.domain.exception.OrganisationNotFoundException;
import com.thinklab.domain.model.Organisation;
import com.thinklab.domain.model.Organisation.ContactRole;
import com.thinklab.domain.model.Organisation.OrganisationUnit.OrganisationUnitStatus;
import com.thinklab.domain.port.HashServicePort;
import com.thinklab.domain.repository.OrganisationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganisationUseCaseTest {

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private HashServicePort hashServicePort;

    private UUID organisationId;
    private Organisation organisation;

    @BeforeEach
    void setUp() {
        organisationId = UUID.randomUUID();
        organisation = Organisation.createNew(organisationId, "Acme Corp", "Acme", "12345678000199", null);
    }

    @Test
    void testInitiateOrganisationUseCase() {
        InitiateOrganisationUseCase useCase = new InitiateOrganisationUseCase(hashServicePort, organisationRepository);
        CreateOrganisationRequest request = new CreateOrganisationRequest(
                "Acme Corp",
                "Acme",
                "12345678000199",
                new CreateOrganisationRequest.BillingDto("billing@acme.com", "USD", "SIMPLES")
        );

        when(hashServicePort.generateSovereignId("organisation-creation")).thenReturn(Mono.just(organisationId));
        when(organisationRepository.create(any(Organisation.class))).thenReturn(Mono.just(organisation));

        StepVerifier.create(useCase.execute(request))
                .assertNext(res -> {
                    assertEquals(organisationId, res.id());
                    assertEquals("Acme Corp", res.corporateName());
                })
                .verifyComplete();
    }

    @Test
    void testRetrieveOrganisationUseCaseSuccess() {
        RetrieveOrganisationUseCase useCase = new RetrieveOrganisationUseCase(organisationRepository);
        when(organisationRepository.findById(organisationId)).thenReturn(Mono.just(organisation));

        StepVerifier.create(useCase.execute(organisationId))
                .assertNext(res -> assertEquals("Acme Corp", res.corporateName()))
                .verifyComplete();
    }

    @Test
    void testRetrieveOrganisationUseCaseNotFound() {
        RetrieveOrganisationUseCase useCase = new RetrieveOrganisationUseCase(organisationRepository);
        when(organisationRepository.findById(organisationId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(organisationId))
                .expectError(OrganisationNotFoundException.class)
                .verify();
    }

    @Test
    void testRetrieveOrganisationsUseCase() {
        RetrieveOrganisationsUseCase useCase = new RetrieveOrganisationsUseCase(organisationRepository);
        when(organisationRepository.findAll()).thenReturn(Flux.just(organisation));

        StepVerifier.create(useCase.execute())
                .assertNext(res -> assertEquals("Acme Corp", res.corporateName()))
                .verifyComplete();
    }

    @Test
    void testUpdateOrganisationUseCase() {
        UpdateOrganisationUseCase useCase = new UpdateOrganisationUseCase(organisationRepository);
        UpdateOrganisationRequest request = new UpdateOrganisationRequest("Acme New", "Trade", "12345678000199");
        when(organisationRepository.updateBasicInfo(organisationId, "Acme New", "Trade", "12345678000199")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(organisationId, request))
                .verifyComplete();
    }

    @Test
    void testControlOrganisationUseCaseActivate() {
        ControlOrganisationUseCase useCase = new ControlOrganisationUseCase(organisationRepository);
        when(organisationRepository.updateStatus(organisationId, Organisation.OrganisationStatus.ACTIVE)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(organisationId, ControlOrganisationUseCase.Action.ACTIVATE))
                .verifyComplete();
    }

    @Test
    void testControlOrganisationUseCaseCancelReplacesDelete() {
        ControlOrganisationUseCase useCase = new ControlOrganisationUseCase(organisationRepository);
        when(organisationRepository.updateStatus(organisationId, Organisation.OrganisationStatus.CANCELED)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(organisationId, ControlOrganisationUseCase.Action.CANCEL))
                .verifyComplete();
    }

    @Test
    void testUpdateOrganisationBillingUseCase() {
        UpdateOrganisationBillingUseCase useCase = new UpdateOrganisationBillingUseCase(organisationRepository);
        UpdateOrganisationBillingRequest request = new UpdateOrganisationBillingRequest("billing@acme.com", "USD", "LUCRO_REAL");
        when(organisationRepository.updateBilling(eq(organisationId), any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(organisationId, request))
                .verifyComplete();
    }

    @Test
    void testInitiateOrganisationUnitUseCase() {
        InitiateOrganisationUnitUseCase useCase = new InitiateOrganisationUnitUseCase(hashServicePort, organisationRepository);
        InitiateOrganisationUnitRequest request = new InitiateOrganisationUnitRequest("Unit 1", "Street", "City", "Country", "12345");
        UUID unitId = UUID.randomUUID();

        when(hashServicePort.generateSovereignId("organisation-unit-creation")).thenReturn(Mono.just(unitId));
        when(organisationRepository.addOrganisationUnit(eq(organisationId), any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(organisationId, request))
                .verifyComplete();
    }

    @Test
    void testControlOrganisationUnitUseCase() {
        ControlOrganisationUnitUseCase useCase = new ControlOrganisationUnitUseCase(organisationRepository);
        UUID unitId = UUID.randomUUID();
        when(organisationRepository.updateOrganisationUnitStatus(organisationId, unitId, OrganisationUnitStatus.SUSPENDED)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(organisationId, unitId, OrganisationUnitStatus.SUSPENDED))
                .verifyComplete();
    }

    @Test
    void testCaptureOrganisationContactUseCase() {
        CaptureOrganisationContactUseCase useCase = new CaptureOrganisationContactUseCase(hashServicePort, organisationRepository);
        CaptureOrganisationContactRequest request = new CaptureOrganisationContactRequest("John", "john@acme.com", "1234", ContactRole.ADMIN);
        UUID contactId = UUID.randomUUID();

        when(hashServicePort.generateSovereignId("contact-creation")).thenReturn(Mono.just(contactId));
        when(organisationRepository.addContact(eq(organisationId), any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(organisationId, request))
                .verifyComplete();
    }
}
