package com.thinklab;

import com.thinklab.application.dto.request.CaptureOrganisationContactRequest;
import com.thinklab.application.dto.request.CreateOrganisationRequest;
import com.thinklab.application.dto.response.OrganisationResponse;
import com.thinklab.application.mapper.OrganisationMapper;
import com.thinklab.application.usecase.ControlOrganisationUnitUseCase;
import com.thinklab.domain.exception.BusinessException;
import com.thinklab.domain.exception.DuplicateOrganisationException;
import com.thinklab.domain.exception.InvalidOrganisationStatusException;
import com.thinklab.domain.exception.OrganisationNotFoundException;
import com.thinklab.domain.model.Organisation;
import com.thinklab.domain.model.Organisation.Billing;
import com.thinklab.domain.model.Organisation.Contact;
import com.thinklab.domain.model.Organisation.ContactRole;
import com.thinklab.domain.model.Organisation.OrganisationStatus;
import com.thinklab.domain.model.Organisation.OrganisationUnit;
import com.thinklab.domain.model.Organisation.OrganisationUnit.OrganisationUnitStatus;
import com.thinklab.domain.repository.OrganisationRepository;
import com.thinklab.infrastructure.adapter.out.persistence.entity.OrganisationDocument;
import com.thinklab.infrastructure.adapter.out.persistence.entity.OrganisationDocument.BillingDocument;
import com.thinklab.infrastructure.adapter.out.persistence.entity.OrganisationDocument.ContactDocument;
import com.thinklab.infrastructure.adapter.out.persistence.entity.OrganisationDocument.OrganisationPersistenceMapper;
import com.thinklab.infrastructure.adapter.out.persistence.entity.OrganisationDocument.OrganisationUnitDocument;
import com.thinklab.infrastructure.config.OpenApiConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Closes the remaining guard, fallback and null-tolerance branches of the service. */
class CoverageCompletionTest {

    private static final UUID ID = UUID.randomUUID();

    private static class TestException extends BusinessException {
        TestException(String code, String message) {
            super(code, message);
        }

        TestException(String code, String message, Throwable cause) {
            super(code, message, cause);
        }
    }

    // --- exceptions ----------------------------------------------------------------------------------------

    @Test
    @DisplayName("BusinessException validates code, message and cause")
    void businessExceptionGuards() {
        Throwable cause = new IllegalStateException("root");
        TestException withCause = new TestException("ERR-X", "boom", cause);
        assertEquals("ERR-X", withCause.getErrorCode());
        assertEquals(cause, withCause.getCause());
        assertThrows(NullPointerException.class, () -> new TestException("ERR-X", "boom", null));
        assertThrows(IllegalArgumentException.class, () -> new TestException(" ", "boom"));
        assertThrows(IllegalArgumentException.class, () -> new TestException("ERR-X", " "));
        assertThrows(NullPointerException.class, () -> new TestException(null, "boom"));
        assertThrows(NullPointerException.class, () -> new TestException("ERR-X", null));
    }

    @Test
    @DisplayName("DuplicateOrganisationException carries the default or a custom code")
    void duplicateException() {
        assertEquals("ERR-ORG-00409", new DuplicateOrganisationException("dup").getErrorCode());
        assertEquals("ERR-CUSTOM", new DuplicateOrganisationException("ERR-CUSTOM", "dup").getErrorCode());
    }

    // --- Organisation aggregate ----------------------------------------------------------------------------

    @Test
    @DisplayName("createNew and reconstitute require the identifying fields")
    void factoryGuards() {
        assertThrows(IllegalArgumentException.class, () -> Organisation.createNew(null, "n", "t", "tax", null));
        assertThrows(IllegalArgumentException.class, () -> Organisation.createNew(ID, null, "t", "tax", null));
        assertThrows(IllegalArgumentException.class, () -> Organisation.createNew(ID, "n", "t", null, null));
        assertThrows(IllegalArgumentException.class,
                () -> Organisation.reconstitute(null, "n", "t", "tax", null, null, null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> Organisation.reconstitute(ID, null, "t", "tax", null, null, null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> Organisation.reconstitute(ID, "n", "t", null, null, null, null, null, null, null));
    }

    @Test
    @DisplayName("reconstitute defaults a missing status, collections and timestamps")
    void reconstituteDefaults() {
        Organisation defaulted = Organisation.reconstitute(ID, "n", "t", "tax", null, null, null, null, null, null);

        assertEquals(OrganisationStatus.PENDING_ACTIVATION, defaulted.getStatus());
        assertTrue(defaulted.getOrganisationUnits().isEmpty());
        assertTrue(defaulted.getContacts().isEmpty());
        assertNotNull(defaulted.getCreatedAt());
        assertEquals(defaulted.getCreatedAt(), defaulted.getUpdatedAt());

        Instant created = Instant.parse("2026-01-01T00:00:00Z");
        Instant updated = Instant.parse("2026-02-01T00:00:00Z");
        OrganisationUnit unit = new OrganisationUnit(ID, "u", "a", "c", "BR", "z", null);
        Contact contact = new Contact(ID, "Ada", "a@b.c", "1", ContactRole.ADMIN);
        Organisation explicit = Organisation.reconstitute(ID, "n", "t", "tax", OrganisationStatus.ACTIVE, null,
                List.of(unit), List.of(contact), created, updated);
        assertEquals(OrganisationStatus.ACTIVE, explicit.getStatus());
        assertEquals(created, explicit.getCreatedAt());
        assertEquals(updated, explicit.getUpdatedAt());
        assertEquals(OrganisationUnitStatus.ACTIVE, explicit.getOrganisationUnits().get(0).status());
    }

    @Test
    @DisplayName("aggregate mutators reject null and blank arguments")
    void mutatorGuards() {
        Organisation org = Organisation.createNew(ID, "n", "t", "tax", null);

        assertThrows(IllegalArgumentException.class, () -> org.updateBasicInfo(null, "t", "tax"));
        assertThrows(IllegalArgumentException.class, () -> org.updateBasicInfo(" ", "t", "tax"));
        assertThrows(IllegalArgumentException.class, () -> org.updateBasicInfo("n", "t", null));
        assertThrows(IllegalArgumentException.class, () -> org.updateBasicInfo("n", "t", " "));
        assertThrows(IllegalArgumentException.class, () -> org.addContact(null));
        assertThrows(IllegalArgumentException.class, () -> org.addOrganisationUnit(null));
        assertThrows(IllegalArgumentException.class, () -> org.updateBilling(null));
        assertThrows(NullPointerException.class, () -> org.suspendUnit(null));
    }

    @Test
    @DisplayName("unit transitions find the unit among several, report a missing one and reject idempotent moves")
    void unitTransitions() {
        Organisation org = Organisation.createNew(ID, "n", "t", "tax", null);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        org.addOrganisationUnit(new OrganisationUnit(first, "a", "a", "c", "BR", "z", OrganisationUnitStatus.ACTIVE));
        org.addOrganisationUnit(new OrganisationUnit(second, "b", "a", "c", "BR", "z", OrganisationUnitStatus.ACTIVE));

        org.suspendUnit(second);
        assertEquals(OrganisationUnitStatus.SUSPENDED, org.getOrganisationUnits().get(1).status());
        assertEquals(OrganisationUnitStatus.ACTIVE, org.getOrganisationUnits().get(0).status());
        assertThrows(InvalidOrganisationStatusException.class, () -> org.suspendUnit(second));
        org.reactivateUnit(second);
        assertThrows(IllegalArgumentException.class, () -> org.suspendUnit(UUID.randomUUID()));
    }

    @Test
    @DisplayName("organisation status transition matrix")
    void statusMatrix() {
        assertTrue(OrganisationStatus.PENDING_ACTIVATION.canTransitionTo(OrganisationStatus.ACTIVE));
        assertTrue(OrganisationStatus.PENDING_ACTIVATION.canTransitionTo(OrganisationStatus.CANCELED));
        assertFalse(OrganisationStatus.PENDING_ACTIVATION.canTransitionTo(OrganisationStatus.SUSPENDED));
        assertTrue(OrganisationStatus.ACTIVE.canTransitionTo(OrganisationStatus.SUSPENDED));
        assertTrue(OrganisationStatus.ACTIVE.canTransitionTo(OrganisationStatus.CANCELED));
        assertFalse(OrganisationStatus.ACTIVE.canTransitionTo(OrganisationStatus.PENDING_ACTIVATION));
        assertTrue(OrganisationStatus.SUSPENDED.canTransitionTo(OrganisationStatus.ACTIVE));
        assertTrue(OrganisationStatus.SUSPENDED.canTransitionTo(OrganisationStatus.CANCELED));
        assertFalse(OrganisationStatus.SUSPENDED.canTransitionTo(OrganisationStatus.PENDING_ACTIVATION));
        for (OrganisationStatus target : OrganisationStatus.values()) {
            assertFalse(OrganisationStatus.CANCELED.canTransitionTo(target));
        }
        assertFalse(OrganisationStatus.ACTIVE.canTransitionTo(null));
        assertThrows(NullPointerException.class, () -> OrganisationStatus.ACTIVE.validateTransitionTo(null));
    }

    // --- application mapper --------------------------------------------------------------------------------

    @Test
    @DisplayName("the mapper is a non-instantiable utility")
    void mapperPrivateConstructor() throws Exception {
        Constructor<OrganisationMapper> constructor = OrganisationMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);

        assertInstanceOf(UnsupportedOperationException.class, thrown.getCause());
    }

    @Test
    @DisplayName("the mapper handles an organisation without billing and a contact without a role")
    void mapperOptionalParts() {
        Organisation org = Organisation.createNew(ID, "n", "t", "tax", null);
        assertNull(OrganisationMapper.toDomain(new CreateOrganisationRequest("n", "t", "tax", null), ID).getBilling());
        org.addContact(new Contact(UUID.randomUUID(), "Ada", "a@b.c", "1", null));

        OrganisationResponse response = OrganisationMapper.toResponse(org);

        assertNull(response.billing());
        assertNull(response.contacts().get(0).role());
    }

    @Test
    @DisplayName("the mapper projects a contact request")
    void mapperContact() {
        Contact contact = OrganisationMapper.toContact(new CaptureOrganisationContactRequest("Ada", "a@b.c", "1", ContactRole.BILLING), ID);

        assertEquals(ContactRole.BILLING, contact.role());
    }

    // --- persistence mapper --------------------------------------------------------------------------------

    @Test
    @DisplayName("the persistence mapper is a non-instantiable utility")
    void persistenceMapperPrivateConstructor() throws Exception {
        Constructor<OrganisationPersistenceMapper> constructor = OrganisationPersistenceMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);

        assertInstanceOf(UnsupportedOperationException.class, thrown.getCause());
    }

    @Test
    @DisplayName("a sparse Mongo document is reconstituted with safe defaults")
    void sparseDocument() {
        OrganisationDocument doc = new OrganisationDocument();
        doc.setId(ID);
        doc.setCorporateName("n");
        doc.setTaxIdentifier("tax");

        Organisation org = OrganisationPersistenceMapper.toDomain(doc);

        assertEquals(OrganisationStatus.PENDING_ACTIVATION, org.getStatus());
        assertNull(org.getBilling());
        assertTrue(org.getOrganisationUnits().isEmpty());
        assertTrue(org.getContacts().isEmpty());
    }

    @Test
    @DisplayName("a document with explicitly null collections is reconstituted with empty ones")
    void nullCollectionsDocument() {
        OrganisationDocument doc = new OrganisationDocument();
        doc.setId(ID);
        doc.setCorporateName("n");
        doc.setTaxIdentifier("tax");
        doc.setOrganisationUnits(null);
        doc.setContacts(null);

        Organisation org = OrganisationPersistenceMapper.toDomain(doc);

        assertTrue(org.getOrganisationUnits().isEmpty());
        assertTrue(org.getContacts().isEmpty());
    }

    @Test
    @DisplayName("a full Mongo document round-trips billing, units (with and without status) and contacts")
    void fullDocument() {
        OrganisationDocument doc = new OrganisationDocument();
        doc.setId(ID);
        doc.setCorporateName("n");
        doc.setTaxIdentifier("tax");
        doc.setStatus("ACTIVE");
        doc.setBilling(new BillingDocument("b@x.c", "BRL", "SIMPLES"));
        doc.setOrganisationUnits(List.of(
                new OrganisationUnitDocument(UUID.randomUUID(), "a", "a", "c", "BR", "z", "SUSPENDED"),
                new OrganisationUnitDocument(UUID.randomUUID(), "b", "a", "c", "BR", "z", null)));
        doc.setContacts(List.of(new ContactDocument(UUID.randomUUID(), "Ada", "a@b.c", "1", "ADMIN")));

        Organisation org = OrganisationPersistenceMapper.toDomain(doc);

        assertEquals(OrganisationStatus.ACTIVE, org.getStatus());
        assertEquals("BRL", org.getBilling().currency());
        assertEquals(OrganisationUnitStatus.SUSPENDED, org.getOrganisationUnits().get(0).status());
        assertEquals(OrganisationUnitStatus.ACTIVE, org.getOrganisationUnits().get(1).status());
        assertEquals(1, OrganisationPersistenceMapper.toDocument(org).getContacts().size());
    }

    @Test
    @DisplayName("an organisation without billing maps to a document without billing")
    void documentWithoutBilling() {
        Organisation org = Organisation.createNew(ID, "n", "t", "tax", null);

        assertNull(OrganisationPersistenceMapper.toDocument(org).getBilling());
    }

    // --- misc ----------------------------------------------------------------------------------------------

    @Test
    @DisplayName("the OpenAPI anchor class is instantiable")
    void openApiAnchor() {
        assertNotNull(new OpenApiConfig());
    }

    @Test
    @DisplayName("reactivating a unit goes through the aggregate and persists the ACTIVE status")
    void reactivateUnitUseCase() {
        OrganisationRepository repository = mock(OrganisationRepository.class);
        Organisation org = Organisation.createNew(ID, "n", "t", "tax", null);
        UUID unitId = UUID.randomUUID();
        org.addOrganisationUnit(new OrganisationUnit(unitId, "a", "a", "c", "BR", "z", OrganisationUnitStatus.SUSPENDED));
        when(repository.findById(ID)).thenReturn(Mono.just(org));
        when(repository.updateOrganisationUnitStatus(any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(new ControlOrganisationUnitUseCase(repository).execute(ID, unitId, OrganisationUnitStatus.ACTIVE))
                .verifyComplete();

        verify(repository).updateOrganisationUnitStatus(ID, unitId, OrganisationUnitStatus.ACTIVE);
    }

    @Test
    @DisplayName("controlling a unit of an unknown organisation is a not-found error")
    void unitOfUnknownOrganisation() {
        OrganisationRepository repository = mock(OrganisationRepository.class);
        when(repository.findById(ID)).thenReturn(Mono.empty());

        StepVerifier.create(new ControlOrganisationUnitUseCase(repository).execute(ID, UUID.randomUUID(), OrganisationUnitStatus.SUSPENDED))
                .expectError(OrganisationNotFoundException.class).verify();
    }
}
