package com.thinklab.infrastructure.adapter.out.persistence;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import com.thinklab.domain.exception.OrganisationNotFoundException;
import com.thinklab.domain.model.Organisation;
import com.thinklab.domain.model.Organisation.Billing;
import com.thinklab.domain.model.Organisation.Contact;
import com.thinklab.domain.model.Organisation.ContactRole;
import com.thinklab.domain.model.Organisation.OrganisationStatus;
import com.thinklab.domain.model.Organisation.OrganisationUnit;
import com.thinklab.domain.model.Organisation.OrganisationUnit.OrganisationUnitStatus;
import com.thinklab.domain.repository.OrganisationRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Organisation aggregate through {@link OrganisationRepository} against a real MongoDB: the POJO codec
 * with nested records (units, contacts, billing), every partial update including the positional
 * {@code organisationUnits.$} update, not-found handling, and the database taken from {@code mongodb.uri}.
 */
@MicronautTest(transactional = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrganisationPersistenceIT implements TestPropertyProvider {

    private static final String DATABASE = "party_reference_it";

    @Override
    public Map<String, String> getProperties() {
        return Map.of("mongodb.uri", MongoContainer.uri(DATABASE));
    }

    @Inject
    OrganisationRepository organisations;

    @Inject
    MongoClient mongoClient;

    private static Organisation newOrganisation() {
        return Organisation.createNew(UUID.randomUUID(), "ThinkLab Ltda", "ThinkLab", "TAX-" + UUID.randomUUID(),
                new Billing("billing@thinklab.com", "BRL", "SIMPLES"));
    }

    private Organisation reload(UUID id) {
        return organisations.findById(id).block();
    }

    @Test
    @DisplayName("a created organisation is read back with its fields, status and billing")
    void createAndFind() {
        Organisation created = organisations.create(newOrganisation()).block();

        Organisation found = reload(created.getId());

        assertEquals(created.getCorporateName(), found.getCorporateName());
        assertEquals(created.getTaxIdentifier(), found.getTaxIdentifier());
        assertEquals(OrganisationStatus.PENDING_ACTIVATION, found.getStatus());
        assertEquals(new Billing("billing@thinklab.com", "BRL", "SIMPLES"), found.getBilling());
        assertTrue(found.getOrganisationUnits().isEmpty());
        assertTrue(found.getContacts().isEmpty());
        assertNotNull(found.getCreatedAt());
    }

    @Test
    @DisplayName("writes land in the database named by mongodb.uri")
    void usesTheConfiguredDatabase() {
        Organisation created = organisations.create(newOrganisation()).block();

        Document stored = Mono.from(mongoClient.getDatabase(DATABASE).getCollection("organisations")
                .find(Filters.eq("_id", created.getId())).first()).block();

        assertNotNull(stored, "organisation not found in " + DATABASE);
    }

    @Test
    @DisplayName("basic info, status and billing partial updates are persisted")
    void partialUpdates() {
        UUID id = organisations.create(newOrganisation()).block().getId();

        organisations.updateBasicInfo(id, "ThinkLab S.A.", "ThinkLab Labs", "TAX-NEW").block();
        organisations.updateStatus(id, OrganisationStatus.ACTIVE).block();
        organisations.updateBilling(id, new Billing("finance@thinklab.com", "USD", "LUCRO_REAL")).block();

        Organisation found = reload(id);
        assertEquals("ThinkLab S.A.", found.getCorporateName());
        assertEquals("ThinkLab Labs", found.getTradeName());
        assertEquals("TAX-NEW", found.getTaxIdentifier());
        assertEquals(OrganisationStatus.ACTIVE, found.getStatus());
        assertEquals(new Billing("finance@thinklab.com", "USD", "LUCRO_REAL"), found.getBilling());
        assertTrue(found.getUpdatedAt().isAfter(found.getCreatedAt()) || found.getUpdatedAt().equals(found.getCreatedAt()));
    }

    @Test
    @DisplayName("units and contacts are appended, and a unit's status is updated in place by its id")
    void unitsAndContacts() {
        UUID id = organisations.create(newOrganisation()).block().getId();
        OrganisationUnit hq = new OrganisationUnit(UUID.randomUUID(), "HQ", "Av. Paulista 1000", "Sao Paulo", "BR", "01310-100", null);
        OrganisationUnit branch = new OrganisationUnit(UUID.randomUUID(), "Branch", "Rua X 1", "Campinas", "BR", "13010-000", null);
        Contact admin = new Contact(UUID.randomUUID(), "Ada Lovelace", "ada@thinklab.com", "+55 11 99999-0000", ContactRole.ADMIN);

        organisations.addOrganisationUnit(id, hq).block();
        organisations.addOrganisationUnit(id, branch).block();
        organisations.addContact(id, admin).block();
        organisations.updateOrganisationUnitStatus(id, branch.unitId(), OrganisationUnitStatus.SUSPENDED).block();

        Organisation found = reload(id);
        assertEquals(List.of(hq, branch.withStatus(OrganisationUnitStatus.SUSPENDED)), found.getOrganisationUnits());
        assertEquals(List.of(admin), found.getContacts());
    }

    @Test
    @DisplayName("updates on an unknown organisation, or an unknown unit, fail with OrganisationNotFoundException")
    void notFound() {
        UUID unknown = UUID.randomUUID();
        UUID id = organisations.create(newOrganisation()).block().getId();

        assertNull(reload(unknown));
        assertThrows(OrganisationNotFoundException.class, () -> organisations.updateStatus(unknown, OrganisationStatus.ACTIVE).block());
        assertThrows(OrganisationNotFoundException.class,
                () -> organisations.updateOrganisationUnitStatus(id, UUID.randomUUID(), OrganisationUnitStatus.SUSPENDED).block());
    }

    @Test
    @DisplayName("findAll returns every stored organisation")
    void findAll() {
        UUID first = organisations.create(newOrganisation()).block().getId();
        UUID second = organisations.create(newOrganisation()).block().getId();

        List<UUID> ids = organisations.findAll().map(Organisation::getId).collectList().block();

        assertTrue(ids.containsAll(List.of(first, second)));
    }
}
