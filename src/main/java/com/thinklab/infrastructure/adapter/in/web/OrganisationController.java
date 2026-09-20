package com.thinklab.infrastructure.adapter.in.web;

import com.thinklab.application.dto.request.CaptureOrganisationContactRequest;
import com.thinklab.application.dto.request.CreateOrganisationRequest;
import com.thinklab.application.dto.request.InitiateOrganisationUnitRequest;
import com.thinklab.application.dto.request.UpdateOrganisationBillingRequest;
import com.thinklab.application.dto.request.UpdateOrganisationRequest;
import com.thinklab.application.dto.response.OrganisationResponse;
import com.thinklab.application.dto.response.OrganisationResponse.OrganisationUnitResponse;
import com.thinklab.application.usecase.CaptureOrganisationContactUseCase;
import com.thinklab.application.usecase.ControlOrganisationUnitUseCase;
import com.thinklab.application.usecase.ControlOrganisationUseCase;
import com.thinklab.application.usecase.InitiateOrganisationUnitUseCase;
import com.thinklab.application.usecase.InitiateOrganisationUseCase;
import com.thinklab.application.usecase.RetrieveOrganisationUseCase;
import com.thinklab.application.usecase.RetrieveOrganisationsUseCase;
import com.thinklab.application.usecase.UpdateOrganisationBillingUseCase;
import com.thinklab.application.usecase.UpdateOrganisationUseCase;
import com.thinklab.domain.exception.OrganisationNotFoundException;
import com.thinklab.domain.model.Organisation.OrganisationUnit.OrganisationUnitStatus;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Inbound Web Adapter for the {@code party-reference-data-directory} Service Domain.
 *
 * <p><b>BIAN-Aligned Resource Model (ADR-013/014):</b>
 * {@link com.thinklab.domain.model.Organisation} is the Control Record; {@code OrganisationUnit}
 * (formerly Branch) is a subordinate, individually addressable Behavior Qualifier Instance Record.
 * Every route follows {@code /party-reference-data-directory/v1/{control-record-id}/{behavior-qualifier}}.
 * There is no {@code DELETE}: {@code control/cancel} is a terminal, soft status transition, never a
 * physical deletion.
 *
 * <p><b>Header-Sourced Forensics (ADR-013):</b> every mutation requires the {@code X-Executor} header,
 * closing this service's previous gap of not capturing an executor identity at all.
 */
@Controller("/party-reference-data-directory/v1")
public class OrganisationController {

    private static final Logger log = LoggerFactory.getLogger(OrganisationController.class);
    private static final String EXECUTOR_HEADER = "X-Executor";

    private final InitiateOrganisationUseCase initiateOrganisationUseCase;
    private final RetrieveOrganisationUseCase retrieveOrganisationUseCase;
    private final RetrieveOrganisationsUseCase retrieveOrganisationsUseCase;
    private final UpdateOrganisationUseCase updateOrganisationUseCase;
    private final ControlOrganisationUseCase controlOrganisationUseCase;
    private final UpdateOrganisationBillingUseCase updateOrganisationBillingUseCase;
    private final InitiateOrganisationUnitUseCase initiateOrganisationUnitUseCase;
    private final ControlOrganisationUnitUseCase controlOrganisationUnitUseCase;
    private final CaptureOrganisationContactUseCase captureOrganisationContactUseCase;

    public OrganisationController(
            InitiateOrganisationUseCase initiateOrganisationUseCase,
            RetrieveOrganisationUseCase retrieveOrganisationUseCase,
            RetrieveOrganisationsUseCase retrieveOrganisationsUseCase,
            UpdateOrganisationUseCase updateOrganisationUseCase,
            ControlOrganisationUseCase controlOrganisationUseCase,
            UpdateOrganisationBillingUseCase updateOrganisationBillingUseCase,
            InitiateOrganisationUnitUseCase initiateOrganisationUnitUseCase,
            ControlOrganisationUnitUseCase controlOrganisationUnitUseCase,
            CaptureOrganisationContactUseCase captureOrganisationContactUseCase
    ) {
        this.initiateOrganisationUseCase = initiateOrganisationUseCase;
        this.retrieveOrganisationUseCase = retrieveOrganisationUseCase;
        this.retrieveOrganisationsUseCase = retrieveOrganisationsUseCase;
        this.updateOrganisationUseCase = updateOrganisationUseCase;
        this.controlOrganisationUseCase = controlOrganisationUseCase;
        this.updateOrganisationBillingUseCase = updateOrganisationBillingUseCase;
        this.initiateOrganisationUnitUseCase = initiateOrganisationUnitUseCase;
        this.controlOrganisationUnitUseCase = controlOrganisationUnitUseCase;
        this.captureOrganisationContactUseCase = captureOrganisationContactUseCase;
    }

    /** Behavior Qualifier: {@code initiate}. Creates a new Organisation Control Record. */
    @Post("/initiate")
    public Mono<HttpResponse<OrganisationResponse>> initiate(
            @Header(EXECUTOR_HEADER) @NotBlank String executor,
            @Body @Valid CreateOrganisationRequest request
    ) {
        log.info("[ACTION: INITIATE_ORGANISATION] [EXECUTOR: {}] Received request to create organisation with tax identifier: {}", executor, request.taxIdentifier());

        return initiateOrganisationUseCase.execute(request)
                .map(HttpResponse::created);
    }

    /** Behavior Qualifier: {@code retrieve}. Fetches a single Organisation by UUID. */
    @Get("/{id}/retrieve")
    public Mono<HttpResponse<OrganisationResponse>> retrieveById(@PathVariable UUID id) {
        log.info("[ACTION: RETRIEVE_ORGANISATION] Received request to get organisation by ID: {}", id);

        return retrieveOrganisationUseCase.execute(id)
                .map(HttpResponse::ok);
    }

    /** Behavior Qualifier: {@code retrieve} (collection). Lists all Organisations. */
    @Get("/retrieve")
    public Flux<OrganisationResponse> retrieveAll() {
        log.info("[ACTION: RETRIEVE_ORGANISATIONS] Received request to list all organisations");

        return retrieveOrganisationsUseCase.execute();
    }

    /** Behavior Qualifier: {@code update}. Updates basic Organisation info. */
    @Put("/{id}/update")
    public Mono<HttpResponse<Void>> update(
            @PathVariable UUID id,
            @Header(EXECUTOR_HEADER) @NotBlank String executor,
            @Body @Valid UpdateOrganisationRequest request
    ) {
        log.info("[ACTION: UPDATE_ORGANISATION] [EXECUTOR: {}] Received request to update organisation info for ID: {}", executor, id);

        return updateOrganisationUseCase.execute(id, request)
                .thenReturn(HttpResponse.noContent());
    }

    /** Behavior Qualifier: {@code control/activate}. */
    @Put("/{id}/control/activate")
    public Mono<HttpResponse<Void>> controlActivate(@PathVariable UUID id, @Header(EXECUTOR_HEADER) @NotBlank String executor) {
        log.info("[ACTION: CONTROL_ORGANISATION] [EXECUTOR: {}] activate for ID: {}", executor, id);

        return controlOrganisationUseCase.execute(id, ControlOrganisationUseCase.Action.ACTIVATE)
                .thenReturn(HttpResponse.noContent());
    }

    /** Behavior Qualifier: {@code control/suspend}. */
    @Put("/{id}/control/suspend")
    public Mono<HttpResponse<Void>> controlSuspend(@PathVariable UUID id, @Header(EXECUTOR_HEADER) @NotBlank String executor) {
        log.info("[ACTION: CONTROL_ORGANISATION] [EXECUTOR: {}] suspend for ID: {}", executor, id);

        return controlOrganisationUseCase.execute(id, ControlOrganisationUseCase.Action.SUSPEND)
                .thenReturn(HttpResponse.noContent());
    }

    /** Behavior Qualifier: {@code control/cancel}. Terminal, soft — replaces the former physical DELETE. */
    @Put("/{id}/control/cancel")
    public Mono<HttpResponse<Void>> controlCancel(@PathVariable UUID id, @Header(EXECUTOR_HEADER) @NotBlank String executor) {
        log.info("[ACTION: CONTROL_ORGANISATION] [EXECUTOR: {}] cancel for ID: {}", executor, id);

        return controlOrganisationUseCase.execute(id, ControlOrganisationUseCase.Action.CANCEL)
                .thenReturn(HttpResponse.noContent());
    }

    /** Behavior Qualifier: {@code billing/update}. */
    @Put("/{id}/billing/update")
    public Mono<HttpResponse<Void>> updateBilling(
            @PathVariable UUID id,
            @Header(EXECUTOR_HEADER) @NotBlank String executor,
            @Body @Valid UpdateOrganisationBillingRequest request
    ) {
        log.info("[ACTION: UPDATE_ORGANISATION_BILLING] [EXECUTOR: {}] Received request to update billing for ID: {}", executor, id);

        return updateOrganisationBillingUseCase.execute(id, request)
                .thenReturn(HttpResponse.noContent());
    }

    /** Behavior Qualifier: {@code organisation-unit/initiate}. */
    @Post("/{id}/organisation-unit/initiate")
    public Mono<HttpResponse<Void>> initiateOrganisationUnit(
            @PathVariable UUID id,
            @Header(EXECUTOR_HEADER) @NotBlank String executor,
            @Body @Valid InitiateOrganisationUnitRequest request
    ) {
        log.info("[ACTION: INITIATE_ORGANISATION_UNIT] [EXECUTOR: {}] Received request to add organisation unit for ID: {}", executor, id);

        return initiateOrganisationUnitUseCase.execute(id, request)
                .thenReturn(HttpResponse.status(HttpStatus.CREATED));
    }

    /** Behavior Qualifier: {@code organisation-unit/retrieve} (collection). Lists all units of an Organisation. */
    @Get("/{id}/organisation-unit/retrieve")
    public Mono<HttpResponse<List<OrganisationUnitResponse>>> retrieveOrganisationUnits(@PathVariable UUID id) {
        log.info("[ACTION: RETRIEVE_ORGANISATION_UNITS] Received request to list organisation units for ID: {}", id);

        return retrieveOrganisationUseCase.execute(id)
                .map(response -> HttpResponse.ok(response.organisationUnits()));
    }

    /** Behavior Qualifier: {@code organisation-unit/{unitId}/retrieve}. */
    @Get("/{id}/organisation-unit/{unitId}/retrieve")
    public Mono<HttpResponse<OrganisationUnitResponse>> retrieveOrganisationUnitById(@PathVariable UUID id, @PathVariable UUID unitId) {
        log.info("[ACTION: RETRIEVE_ORGANISATION_UNIT] Received request to get organisation unit {} for organisation ID: {}", unitId, id);

        return retrieveOrganisationUseCase.execute(id)
                .flatMap(response -> response.organisationUnits().stream()
                        .filter(u -> u.unitId().equals(unitId))
                        .findFirst()
                        .map(unit -> Mono.just(HttpResponse.ok(unit)))
                        .orElseGet(() -> Mono.error(new OrganisationNotFoundException(
                                String.format("OrganisationUnit with ID [%s] could not be found under Organisation [%s].", unitId, id)))));
    }

    /** Behavior Qualifier: {@code organisation-unit/control/suspend}. */
    @Put("/{id}/organisation-unit/{unitId}/control/suspend")
    public Mono<HttpResponse<Void>> controlOrganisationUnitSuspend(
            @PathVariable UUID id, @PathVariable UUID unitId, @Header(EXECUTOR_HEADER) @NotBlank String executor
    ) {
        log.info("[ACTION: CONTROL_ORGANISATION_UNIT] [EXECUTOR: {}] suspend for organisation {} / unit {}", executor, id, unitId);

        return controlOrganisationUnitUseCase.execute(id, unitId, OrganisationUnitStatus.SUSPENDED)
                .thenReturn(HttpResponse.noContent());
    }

    /** Behavior Qualifier: {@code organisation-unit/control/reactivate}. */
    @Put("/{id}/organisation-unit/{unitId}/control/reactivate")
    public Mono<HttpResponse<Void>> controlOrganisationUnitReactivate(
            @PathVariable UUID id, @PathVariable UUID unitId, @Header(EXECUTOR_HEADER) @NotBlank String executor
    ) {
        log.info("[ACTION: CONTROL_ORGANISATION_UNIT] [EXECUTOR: {}] reactivate for organisation {} / unit {}", executor, id, unitId);

        return controlOrganisationUnitUseCase.execute(id, unitId, OrganisationUnitStatus.ACTIVE)
                .thenReturn(HttpResponse.noContent());
    }

    /** Behavior Qualifier: {@code contact/initiate}. */
    @Post("/{id}/contact/initiate")
    public Mono<HttpResponse<Void>> initiateContact(
            @PathVariable UUID id,
            @Header(EXECUTOR_HEADER) @NotBlank String executor,
            @Body @Valid CaptureOrganisationContactRequest request
    ) {
        log.info("[ACTION: CAPTURE_ORGANISATION_CONTACT] [EXECUTOR: {}] Received request to add contact for ID: {}", executor, id);

        return captureOrganisationContactUseCase.execute(id, request)
                .thenReturn(HttpResponse.status(HttpStatus.CREATED));
    }

    /** Behavior Qualifier: {@code contact/retrieve} (collection). Lists all contacts of an Organisation. */
    @Get("/{id}/contact/retrieve")
    public Mono<HttpResponse<List<OrganisationResponse.ContactResponse>>> retrieveContacts(@PathVariable UUID id) {
        log.info("[ACTION: RETRIEVE_ORGANISATION_CONTACTS] Received request to list contacts for ID: {}", id);

        return retrieveOrganisationUseCase.execute(id)
                .map(response -> HttpResponse.ok(response.contacts()));
    }
}
