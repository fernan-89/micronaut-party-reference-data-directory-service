package com.thinklab.domain.model;

import com.thinklab.domain.model.Company.Billing;
import com.thinklab.domain.model.Company.Branch;
import com.thinklab.domain.model.Company.CompanyStatus;
import com.thinklab.domain.model.Company.Contact;
import com.thinklab.domain.model.Company.ContactRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CompanyValueObjectsTest {

    @Test
    @DisplayName("Branch Value Object invariants and equality")
    void testBranchValueObject() {
        UUID branchId = UUID.randomUUID();
        Branch branch1 = new Branch(branchId, "Branch Alpha", "Street 1", "London", "UK", "EC1A 1BB");
        Branch branch2 = new Branch(branchId, "Branch Alpha", "Street 1", "London", "UK", "EC1A 1BB");

        assertEquals(branch1, branch2);
        assertEquals(branch1.hashCode(), branch2.hashCode());
        assertEquals("Branch Alpha", branch1.branchName());
        assertEquals("Street 1", branch1.address());
        assertEquals("London", branch1.city());
        assertEquals("UK", branch1.country());
        assertEquals("EC1A 1BB", branch1.zipCode());
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
    @DisplayName("CompanyStatus state transitions validation")
    void testCompanyStatusTransitions() {
        assertEquals(CompanyStatus.PENDING_ACTIVATION, CompanyStatus.valueOf("PENDING_ACTIVATION"));
        assertEquals(CompanyStatus.ACTIVE, CompanyStatus.valueOf("ACTIVE"));
        assertEquals(CompanyStatus.SUSPENDED, CompanyStatus.valueOf("SUSPENDED"));
        assertEquals(CompanyStatus.CANCELED, CompanyStatus.valueOf("CANCELED"));

        Company company = Company.createNew(UUID.randomUUID(), "Corp", "Trade", "11.222.333/0001-44", null);
        assertEquals(CompanyStatus.PENDING_ACTIVATION, company.getStatus());

        company.activate();
        assertEquals(CompanyStatus.ACTIVE, company.getStatus());

        company.suspend();
        assertEquals(CompanyStatus.SUSPENDED, company.getStatus());
    }

    @Test
    @DisplayName("Company entity updateBilling and edge cases")
    void testUpdateBillingAndLists() {
        Company company = Company.createNew(UUID.randomUUID(), "Corp A", "Trade A", "11.222.333/0001-44", null);
        Billing billing = new Billing("bill@corp.com", "BRL", "LUCRO_REAL");

        company.updateBilling(billing);
        assertEquals(billing, company.getBilling());

        UUID branchId = UUID.randomUUID();
        Branch branch = new Branch(branchId, "Branch B", "Addr", "City", "BR", "12345");
        company.addBranch(branch);
        assertEquals(1, company.getBranches().size());

        UUID contactId = UUID.randomUUID();
        Contact contact = new Contact(contactId, "Bob", "bob@corp.com", "123", ContactRole.OPERATIONS);
        company.addContact(contact);
        assertEquals(1, company.getContacts().size());
    }
}
