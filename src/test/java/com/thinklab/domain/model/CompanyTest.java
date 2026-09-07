package com.thinklab.domain.model;

import com.thinklab.domain.model.Company.Billing;
import com.thinklab.domain.model.Company.Branch;
import com.thinklab.domain.model.Company.CompanyStatus;
import com.thinklab.domain.model.Company.Contact;
import com.thinklab.domain.model.Company.ContactRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CompanyTest {

    @Test
    void testCreateNewCompanySuccess() {
        UUID id = UUID.randomUUID();
        Billing billing = new Billing("finance@acme.com", "USD", "LUCRO_REAL");
        Company company = Company.createNew(id, "Acme Inc", "Acme", "12345678000199", billing);

        assertEquals(id, company.getId());
        assertEquals("Acme Inc", company.getCorporateName());
        assertEquals("Acme", company.getTradeName());
        assertEquals("12345678000199", company.getTaxIdentifier());
        assertEquals(CompanyStatus.PENDING_ACTIVATION, company.getStatus());
        assertNotNull(company.getCreatedAt());
        assertEquals(billing, company.getBilling());
        assertTrue(company.getBranches().isEmpty());
        assertTrue(company.getContacts().isEmpty());
    }

    @Test
    void testCreateNewCompanyValidation() {
        assertThrows(IllegalArgumentException.class, () ->
                Company.createNew(null, "Acme", "Acme", "12345678000199", null));
    }

    @Test
    void testActivateAndSuspendCompany() {
        UUID id = UUID.randomUUID();
        Company company = Company.createNew(id, "Acme Inc", "Acme", "12345678000199", null);

        company.activate();
        assertEquals(CompanyStatus.ACTIVE, company.getStatus());

        company.suspend();
        assertEquals(CompanyStatus.SUSPENDED, company.getStatus());

        // Cannot activate suspended company directly without domain rules
        assertThrows(IllegalStateException.class, company::activate);
    }

    @Test
    void testAddBranchAndContact() {
        UUID id = UUID.randomUUID();
        Company company = Company.createNew(id, "Acme Inc", "Acme", "12345678000199", null);

        Branch branch = new Branch(UUID.randomUUID(), "Branch A", "Street 1", "City", "Country", "12345");
        company.addBranch(branch);
        assertEquals(1, company.getBranches().size());

        Contact contact = new Contact(UUID.randomUUID(), "John", "john@acme.com", "1234", ContactRole.ADMIN);
        company.addContact(contact);
        assertEquals(1, company.getContacts().size());
    }

    @Test
    void testUpdateBasicInfo() {
        UUID id = UUID.randomUUID();
        Company company = Company.createNew(id, "Acme Inc", "Acme", "12345678000199", null);

        company.updateBasicInfo("Acme Global Inc", "Acme Global", "99999999000100");
        assertEquals("Acme Global Inc", company.getCorporateName());
        assertEquals("Acme Global", company.getTradeName());
        assertEquals("99999999000100", company.getTaxIdentifier());
    }
}
