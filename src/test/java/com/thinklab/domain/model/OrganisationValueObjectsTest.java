package com.thinklab.domain.model;

import com.thinklab.domain.model.Organisation.Billing;
import com.thinklab.domain.model.Organisation.Contact;
import com.thinklab.domain.model.Organisation.ContactRole;
import com.thinklab.domain.model.Organisation.OrganisationStatus;
import com.thinklab.domain.model.Organisation.OrganisationUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrganisationValueObjectsTest {

    @Test
    @DisplayName("OrganisationUnit Value Object invariants, equality, and default status")
    void testOrganisationUnitValueObject() {
        UUID unitId = UUID.randomUUID();
        OrganisationUnit unit1 = new OrganisationUnit(unitId, "Unit Alpha", "Street 1", "London", "UK", "EC1A 1BB", OrganisationUnit.OrganisationUnitStatus.ACTIVE);
        OrganisationUnit unit2 = new OrganisationUnit(unitId, "Unit Alpha", "Street 1", "London", "UK", "EC1A 1BB", OrganisationUnit.OrganisationUnitStatus.ACTIVE);

        assertEquals(unit1, unit2);
        assertEquals(unit1.hashCode(), unit2.hashCode());
        assertEquals("Unit Alpha", unit1.unitName());
        assertEquals("Street 1", unit1.address());
        assertEquals("London", unit1.city());
        assertEquals("UK", unit1.country());
        assertEquals("EC1A 1BB", unit1.zipCode());
        assertEquals(OrganisationUnit.OrganisationUnitStatus.ACTIVE, unit1.status());

        // Null status defaults to ACTIVE
        OrganisationUnit unitWithNullStatus = new OrganisationUnit(unitId, "Unit Beta", "Street 2", "Paris", "FR", "75001", null);
        assertEquals(OrganisationUnit.OrganisationUnitStatus.ACTIVE, unitWithNullStatus.status());

        OrganisationUnit suspended = unit1.withStatus(OrganisationUnit.OrganisationUnitStatus.SUSPENDED);
        assertEquals(OrganisationUnit.OrganisationUnitStatus.SUSPENDED, suspended.status());
        assertEquals(unit1.unitId(), suspended.unitId());
    }

    @Test
    @DisplayName("Contact Value Object and ContactRole enum invariants")
    void testContactValueObject() {
        UUID contactId = UUID.randomUUID();
        Contact contact = new Contact(contactId, "Alice Smith", "alice@thinklab.com", "+44-20-7946-0999", ContactRole.TECHNICAL);

        assertEquals(contactId, contact.contactId());
        assertEquals("Alice Smith", contact.fullName());
        assertEquals("alice@thinklab.com", contact.email());
        assertEquals("+44-20-7946-0999", contact.phoneNumber());
        assertEquals(ContactRole.TECHNICAL, contact.role());

        assertEquals(ContactRole.ADMIN, ContactRole.valueOf("ADMIN"));
        assertEquals(ContactRole.BILLING, ContactRole.valueOf("BILLING"));
        assertEquals(ContactRole.OPERATIONS, ContactRole.valueOf("OPERATIONS"));
        assertEquals(ContactRole.TECHNICAL, ContactRole.valueOf("TECHNICAL"));
    }

    @Test
    @DisplayName("Billing Value Object invariants")
    void testBillingValueObject() {
        Billing billing = new Billing("accounts@thinklab.com", "EUR", "SIMPLES");

        assertEquals("accounts@thinklab.com", billing.billingEmail());
        assertEquals("EUR", billing.currency());
        assertEquals("SIMPLES", billing.taxRegime());
    }

    @Test
    @DisplayName("OrganisationStatus state machine transitions")
    void testOrganisationStatusTransitions() {
        assertEquals(OrganisationStatus.PENDING_ACTIVATION, OrganisationStatus.valueOf("PENDING_ACTIVATION"));
        assertEquals(OrganisationStatus.ACTIVE, OrganisationStatus.valueOf("ACTIVE"));
        assertEquals(OrganisationStatus.SUSPENDED, OrganisationStatus.valueOf("SUSPENDED"));
        assertEquals(OrganisationStatus.CANCELED, OrganisationStatus.valueOf("CANCELED"));

        Organisation organisation = Organisation.createNew(UUID.randomUUID(), "Corp", "Trade", "11.222.333/0001-44", null);
        assertEquals(OrganisationStatus.PENDING_ACTIVATION, organisation.getStatus());

        organisation.activate();
        assertEquals(OrganisationStatus.ACTIVE, organisation.getStatus());

        organisation.suspend();
        assertEquals(OrganisationStatus.SUSPENDED, organisation.getStatus());

        assertFalse(OrganisationStatus.CANCELED.canTransitionTo(OrganisationStatus.ACTIVE));
    }

    @Test
    @DisplayName("Organisation entity updateBilling and edge cases")
    void testUpdateBillingAndLists() {
        Organisation organisation = Organisation.createNew(UUID.randomUUID(), "Corp A", "Trade A", "11.222.333/0001-44", null);
        Billing billing = new Billing("bill@corp.com", "BRL", "LUCRO_REAL");

        organisation.updateBilling(billing);
        assertEquals(billing, organisation.getBilling());

        UUID unitId = UUID.randomUUID();
        OrganisationUnit unit = new OrganisationUnit(unitId, "Unit B", "Addr", "City", "BR", "12345", OrganisationUnit.OrganisationUnitStatus.ACTIVE);
        organisation.addOrganisationUnit(unit);
        assertEquals(1, organisation.getOrganisationUnits().size());

        UUID contactId = UUID.randomUUID();
        Contact contact = new Contact(contactId, "Bob", "bob@corp.com", "123", ContactRole.OPERATIONS);
        organisation.addContact(contact);
        assertEquals(1, organisation.getContacts().size());
    }
}
