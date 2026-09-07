package com.thinklab.infrastructure.adapter.out.persistence.entity;

import com.thinklab.domain.model.Company;
import com.thinklab.domain.model.Company.Billing;
import com.thinklab.domain.model.Company.Branch;
import com.thinklab.domain.model.Company.CompanyStatus;
import com.thinklab.domain.model.Company.Contact;
import com.thinklab.domain.model.Company.ContactRole;
import com.thinklab.infrastructure.adapter.out.persistence.entity.CompanyDocument.CompanyPersistenceMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CompanyDocumentTest {

    @Test
    void testToDocumentAndToDomainMapping() {
        UUID companyId = UUID.randomUUID();
        Billing billing = new Billing("finance@thinklab.com", "USD", "LUCRO_PRESUMIDO");
        Company company = Company.createNew(companyId, "ThinkLab Corp", "ThinkLab", "12345678000190", billing);

        UUID branchId = UUID.randomUUID();
        company.addBranch(new Branch(branchId, "Headquarters", "Av Paulista 1000", "Sao Paulo", "Brazil", "01310-100"));

        UUID contactId = UUID.randomUUID();
        company.addContact(new Contact(contactId, "Jane Doe", "jane@thinklab.com", "+5511999999999", ContactRole.TECHNICAL));

        company.activate();

        // 1. Domain -> Document
        CompanyDocument doc = CompanyPersistenceMapper.toDocument(company);

        assertNotNull(doc);
        assertEquals(companyId, doc.getId());
        assertEquals("ThinkLab Corp", doc.getCorporateName());
        assertEquals("ThinkLab", doc.getTradeName());
        assertEquals("12345678000190", doc.getTaxIdentifier());
        assertEquals("ACTIVE", doc.getStatus());
        assertNotNull(doc.getBilling());
        assertEquals("finance@thinklab.com", doc.getBilling().billingEmail());
        assertEquals(1, doc.getBranches().size());
        assertEquals("Headquarters", doc.getBranches().get(0).branchName());
        assertEquals(1, doc.getContacts().size());
        assertEquals("Jane Doe", doc.getContacts().get(0).fullName());

        // 2. Document -> Domain
        Company mappedDomain = CompanyPersistenceMapper.toDomain(doc);

        assertNotNull(mappedDomain);
        assertEquals(companyId, mappedDomain.getId());
        assertEquals("ThinkLab Corp", mappedDomain.getCorporateName());
        assertEquals("ThinkLab", mappedDomain.getTradeName());
        assertEquals("12345678000190", mappedDomain.getTaxIdentifier());
        assertEquals(CompanyStatus.ACTIVE, mappedDomain.getStatus());
        assertNotNull(mappedDomain.getBilling());
        assertEquals("finance@thinklab.com", mappedDomain.getBilling().billingEmail());
        assertEquals(1, mappedDomain.getBranches().size());
        assertEquals("Headquarters", mappedDomain.getBranches().get(0).branchName());
        assertEquals(1, mappedDomain.getContacts().size());
        assertEquals("Jane Doe", mappedDomain.getContacts().get(0).fullName());
    }
}
