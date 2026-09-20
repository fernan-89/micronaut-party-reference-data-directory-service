package com.thinklab.domain.model;

import com.thinklab.domain.exception.InvalidOrganisationStatusException;
import com.thinklab.domain.model.Organisation.Billing;
import com.thinklab.domain.model.Organisation.Contact;
import com.thinklab.domain.model.Organisation.ContactRole;
import com.thinklab.domain.model.Organisation.OrganisationStatus;
import com.thinklab.domain.model.Organisation.OrganisationUnit;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrganisationTest {

    @Test
    void testCreateNewOrganisationSuccess() {
        UUID id = UUID.randomUUID();
        Billing billing = new Billing("finance@acme.com", "USD", "LUCRO_REAL");
        Organisation organisation = Organisation.createNew(id, "Acme Inc", "Acme", "12345678000199", billing);

        assertEquals(id, organisation.getId());
        assertEquals("Acme Inc", organisation.getCorporateName());
        assertEquals("Acme", organisation.getTradeName());
        assertEquals("12345678000199", organisation.getTaxIdentifier());
        assertEquals(OrganisationStatus.PENDING_ACTIVATION, organisation.getStatus());
        assertNotNull(organisation.getCreatedAt());
        assertEquals(billing, organisation.getBilling());
        assertTrue(organisation.getOrganisationUnits().isEmpty());
        assertTrue(organisation.getContacts().isEmpty());
    }

    @Test
    void testCreateNewOrganisationValidation() {
        assertThrows(IllegalArgumentException.class, () ->
                Organisation.createNew(null, "Acme", "Acme", "12345678000199", null));
    }

    @Test
    void testActivateAndSuspendOrganisation() {
        UUID id = UUID.randomUUID();
        Organisation organisation = Organisation.createNew(id, "Acme Inc", "Acme", "12345678000199", null);

        organisation.activate();
        assertEquals(OrganisationStatus.ACTIVE, organisation.getStatus());

        organisation.suspend();
        assertEquals(OrganisationStatus.SUSPENDED, organisation.getStatus());

        // Suspended -> Active is a legal transition (unlike the legacy Company state machine)
        organisation.activate();
        assertEquals(OrganisationStatus.ACTIVE, organisation.getStatus());
    }

    @Test
    void testCancelIsTerminal() {
        Organisation organisation = Organisation.createNew(UUID.randomUUID(), "Acme Inc", "Acme", "12345678000199", null);

        organisation.cancel();
        assertEquals(OrganisationStatus.CANCELED, organisation.getStatus());

        assertThrows(InvalidOrganisationStatusException.class, organisation::activate,
                "Zero Trust Violation: no transition is permitted out of terminal CANCELED state.");
    }

    @Test
    void testAddOrganisationUnitAndContact() {
        UUID id = UUID.randomUUID();
        Organisation organisation = Organisation.createNew(id, "Acme Inc", "Acme", "12345678000199", null);

        OrganisationUnit unit = new OrganisationUnit(UUID.randomUUID(), "Unit A", "Street 1", "City", "Country", "12345", OrganisationUnit.OrganisationUnitStatus.ACTIVE);
        organisation.addOrganisationUnit(unit);
        assertEquals(1, organisation.getOrganisationUnits().size());

        Contact contact = new Contact(UUID.randomUUID(), "John", "john@acme.com", "1234", ContactRole.ADMIN);
        organisation.addContact(contact);
        assertEquals(1, organisation.getContacts().size());
    }

    @Test
    void testSuspendAndReactivateOrganisationUnit() {
        Organisation organisation = Organisation.createNew(UUID.randomUUID(), "Acme Inc", "Acme", "12345678000199", null);
        UUID unitId = UUID.randomUUID();
        organisation.addOrganisationUnit(new OrganisationUnit(unitId, "Unit A", "Street 1", "City", "Country", "12345", OrganisationUnit.OrganisationUnitStatus.ACTIVE));

        organisation.suspendUnit(unitId);
        assertEquals(OrganisationUnit.OrganisationUnitStatus.SUSPENDED, organisation.getOrganisationUnits().get(0).status());

        organisation.reactivateUnit(unitId);
        assertEquals(OrganisationUnit.OrganisationUnitStatus.ACTIVE, organisation.getOrganisationUnits().get(0).status());
    }

    @Test
    void testSuspendUnknownUnitFails() {
        Organisation organisation = Organisation.createNew(UUID.randomUUID(), "Acme Inc", "Acme", "12345678000199", null);

        assertThrows(IllegalArgumentException.class, () -> organisation.suspendUnit(UUID.randomUUID()));
    }

    @Test
    void testUpdateBasicInfo() {
        UUID id = UUID.randomUUID();
        Organisation organisation = Organisation.createNew(id, "Acme Inc", "Acme", "12345678000199", null);

        organisation.updateBasicInfo("Acme Global Inc", "Acme Global", "99999999000100");
        assertEquals("Acme Global Inc", organisation.getCorporateName());
        assertEquals("Acme Global", organisation.getTradeName());
        assertEquals("99999999000100", organisation.getTaxIdentifier());
    }
}
