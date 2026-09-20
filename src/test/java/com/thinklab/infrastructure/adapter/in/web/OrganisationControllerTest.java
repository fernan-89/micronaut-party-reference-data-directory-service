package com.thinklab.infrastructure.adapter.in.web;

import com.thinklab.application.dto.request.CaptureOrganisationContactRequest;
import com.thinklab.application.dto.request.CreateOrganisationRequest;
import com.thinklab.application.dto.request.InitiateOrganisationUnitRequest;
import com.thinklab.application.dto.request.UpdateOrganisationBillingRequest;
import com.thinklab.application.dto.request.UpdateOrganisationRequest;
import com.thinklab.application.dto.response.OrganisationResponse;
import com.thinklab.application.usecase.CaptureOrganisationContactUseCase;
import com.thinklab.application.usecase.ControlOrganisationUnitUseCase;
import com.thinklab.application.usecase.ControlOrganisationUseCase;
import com.thinklab.application.usecase.InitiateOrganisationUnitUseCase;
import com.thinklab.application.usecase.InitiateOrganisationUseCase;
import com.thinklab.application.usecase.RetrieveOrganisationUseCase;
import com.thinklab.application.usecase.RetrieveOrganisationsUseCase;
import com.thinklab.application.usecase.UpdateOrganisationBillingUseCase;
import com.thinklab.application.usecase.UpdateOrganisationUseCase;
import com.thinklab.domain.model.Organisation.ContactRole;
import com.thinklab.domain.model.Organisation.OrganisationUnit.OrganisationUnitStatus;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganisationControllerTest {

    @Mock private InitiateOrganisationUseCase initiateOrganisationUseCase;
    @Mock private RetrieveOrganisationUseCase retrieveOrganisationUseCase;
    @Mock private RetrieveOrganisationsUseCase retrieveOrganisationsUseCase;
    @Mock private UpdateOrganisationUseCase updateOrganisationUseCase;
    @Mock private ControlOrganisationUseCase controlOrganisationUseCase;
    @Mock private UpdateOrganisationBillingUseCase updateOrganisationBillingUseCase;
    @Mock private InitiateOrganisationUnitUseCase initiateOrganisationUnitUseCase;
    @Mock private ControlOrganisationUnitUseCase controlOrganisationUnitUseCase;
    @Mock private CaptureOrganisationContactUseCase captureOrganisationContactUseCase;

    @InjectMocks
    private OrganisationController organisationController;

    private UUID organisationId;
    private OrganisationResponse sampleResponse;
    private static final String EXECUTOR = "security-admin";

    @BeforeEach
    void setUp() {
        organisationId = UUID.randomUUID();
        sampleResponse = new OrganisationResponse(
                organisationId,
                "Acme Corp",
                "Acme",
                "12345678000199",
                "ACTIVE",
                new OrganisationResponse.BillingResponse("billing@acme.com", "USD", "SIMPLES"),
                Collections.emptyList(),
                Collections.emptyList(),
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    void testInitiate() {
        CreateOrganisationRequest request = new CreateOrganisationRequest(
                "Acme Corp", "Acme", "12345678000199",
                new CreateOrganisationRequest.BillingDto("billing@acme.com", "USD", "SIMPLES")
        );

        when(initiateOrganisationUseCase.execute(any(CreateOrganisationRequest.class))).thenReturn(Mono.just(sampleResponse));

        StepVerifier.create(organisationController.initiate(EXECUTOR, request))
                .assertNext(response -> {
                    assertEquals(HttpStatus.CREATED, response.getStatus());
                    assertNotNull(response.body());
                    assertEquals("Acme Corp", response.body().corporateName());
                })
                .verifyComplete();
    }

    @Test
    void testRetrieveById() {
        when(retrieveOrganisationUseCase.execute(organisationId)).thenReturn(Mono.just(sampleResponse));

        StepVerifier.create(organisationController.retrieveById(organisationId))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatus());
                    assertNotNull(response.body());
                    assertEquals(organisationId, response.body().id());
                })
                .verifyComplete();
    }

    @Test
    void testRetrieveAll() {
        when(retrieveOrganisationsUseCase.execute()).thenReturn(Flux.just(sampleResponse));

        StepVerifier.create(organisationController.retrieveAll())
                .assertNext(res -> assertEquals("Acme Corp", res.corporateName()))
                .verifyComplete();
    }

    @Test
    void testUpdate() {
        UpdateOrganisationRequest request = new UpdateOrganisationRequest("Acme Corp New", "Acme New", "12345678000199");
        when(updateOrganisationUseCase.execute(eq(organisationId), any(UpdateOrganisationRequest.class))).thenReturn(Mono.empty());

        StepVerifier.create(organisationController.update(organisationId, EXECUTOR, request))
                .assertNext(response -> assertEquals(HttpStatus.NO_CONTENT, response.getStatus()))
                .verifyComplete();
    }

    @Test
    void testControlActivate() {
        when(controlOrganisationUseCase.execute(organisationId, ControlOrganisationUseCase.Action.ACTIVATE)).thenReturn(Mono.empty());

        StepVerifier.create(organisationController.controlActivate(organisationId, EXECUTOR))
                .assertNext(response -> assertEquals(HttpStatus.NO_CONTENT, response.getStatus()))
                .verifyComplete();
    }

    @Test
    void testControlSuspend() {
        when(controlOrganisationUseCase.execute(organisationId, ControlOrganisationUseCase.Action.SUSPEND)).thenReturn(Mono.empty());

        StepVerifier.create(organisationController.controlSuspend(organisationId, EXECUTOR))
                .assertNext(response -> assertEquals(HttpStatus.NO_CONTENT, response.getStatus()))
                .verifyComplete();
    }

    @Test
    void testControlCancelReplacesDelete() {
        when(controlOrganisationUseCase.execute(organisationId, ControlOrganisationUseCase.Action.CANCEL)).thenReturn(Mono.empty());

        StepVerifier.create(organisationController.controlCancel(organisationId, EXECUTOR))
                .assertNext(response -> assertEquals(HttpStatus.NO_CONTENT, response.getStatus()))
                .verifyComplete();
    }

    @Test
    void testUpdateBilling() {
        UpdateOrganisationBillingRequest request = new UpdateOrganisationBillingRequest("new@acme.com", "BRL", "LUCRO_REAL");
        when(updateOrganisationBillingUseCase.execute(eq(organisationId), any(UpdateOrganisationBillingRequest.class))).thenReturn(Mono.empty());

        StepVerifier.create(organisationController.updateBilling(organisationId, EXECUTOR, request))
                .assertNext(response -> assertEquals(HttpStatus.NO_CONTENT, response.getStatus()))
                .verifyComplete();
    }

    @Test
    void testInitiateOrganisationUnit() {
        InitiateOrganisationUnitRequest request = new InitiateOrganisationUnitRequest("Unit 1", "Main St 100", "City", "Country", "12345");
        when(initiateOrganisationUnitUseCase.execute(eq(organisationId), any(InitiateOrganisationUnitRequest.class))).thenReturn(Mono.empty());

        StepVerifier.create(organisationController.initiateOrganisationUnit(organisationId, EXECUTOR, request))
                .assertNext(response -> assertEquals(HttpStatus.CREATED, response.getStatus()))
                .verifyComplete();
    }

    @Test
    void testRetrieveOrganisationUnits() {
        when(retrieveOrganisationUseCase.execute(organisationId)).thenReturn(Mono.just(sampleResponse));

        StepVerifier.create(organisationController.retrieveOrganisationUnits(organisationId))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatus());
                    assertEquals(0, response.body().size());
                })
                .verifyComplete();
    }

    @Test
    void testRetrieveOrganisationUnitByIdNotFound() {
        when(retrieveOrganisationUseCase.execute(organisationId)).thenReturn(Mono.just(sampleResponse));

        StepVerifier.create(organisationController.retrieveOrganisationUnitById(organisationId, UUID.randomUUID()))
                .expectError(com.thinklab.domain.exception.OrganisationNotFoundException.class)
                .verify();
    }

    @Test
    void testControlOrganisationUnitSuspend() {
        UUID unitId = UUID.randomUUID();
        when(controlOrganisationUnitUseCase.execute(organisationId, unitId, OrganisationUnitStatus.SUSPENDED)).thenReturn(Mono.empty());

        StepVerifier.create(organisationController.controlOrganisationUnitSuspend(organisationId, unitId, EXECUTOR))
                .assertNext(response -> assertEquals(HttpStatus.NO_CONTENT, response.getStatus()))
                .verifyComplete();
    }

    @Test
    void testControlOrganisationUnitReactivate() {
        UUID unitId = UUID.randomUUID();
        when(controlOrganisationUnitUseCase.execute(organisationId, unitId, OrganisationUnitStatus.ACTIVE)).thenReturn(Mono.empty());

        StepVerifier.create(organisationController.controlOrganisationUnitReactivate(organisationId, unitId, EXECUTOR))
                .assertNext(response -> assertEquals(HttpStatus.NO_CONTENT, response.getStatus()))
                .verifyComplete();
    }

    @Test
    void testInitiateContact() {
        CaptureOrganisationContactRequest request = new CaptureOrganisationContactRequest("John Doe", "john@acme.com", "+123456789", ContactRole.ADMIN);
        when(captureOrganisationContactUseCase.execute(eq(organisationId), any(CaptureOrganisationContactRequest.class))).thenReturn(Mono.empty());

        StepVerifier.create(organisationController.initiateContact(organisationId, EXECUTOR, request))
                .assertNext(response -> assertEquals(HttpStatus.CREATED, response.getStatus()))
                .verifyComplete();
    }

    @Test
    void testRetrieveContacts() {
        when(retrieveOrganisationUseCase.execute(organisationId)).thenReturn(Mono.just(sampleResponse));

        StepVerifier.create(organisationController.retrieveContacts(organisationId))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatus());
                    assertEquals(0, response.body().size());
                })
                .verifyComplete();
    }
}
