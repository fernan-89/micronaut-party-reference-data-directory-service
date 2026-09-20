package com.thinklab.infrastructure.adapter.out.persistence.entity;

import com.thinklab.domain.model.Organisation;
import com.thinklab.domain.model.Organisation.Billing;
import com.thinklab.domain.model.Organisation.Contact;
import com.thinklab.domain.model.Organisation.ContactRole;
import com.thinklab.domain.model.Organisation.OrganisationStatus;
import com.thinklab.domain.model.Organisation.OrganisationUnit;
import com.thinklab.infrastructure.adapter.out.persistence.entity.OrganisationDocument.OrganisationPersistenceMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrganisationDocumentTest {

    @Test
    void testToDocumentAndToDomainMapping() {
        UUID organisationId = UUID.randomUUID();
        Billing billing = new Billing("finance@thinklab.com", "USD", "LUCRO_PRESUMIDO");
        Organisation organisation = Organisation.createNew(organisationId, "ThinkLab Corp", "ThinkLab", "12345678000190", billing);

        UUID unitId = UUID.randomUUID();
        organisation.addOrganisationUnit(new OrganisationUnit(unitId, "Headquarters", "Av Paulista 1000", "Sao Paulo", "Brazil", "01310-100", OrganisationUnit.OrganisationUnitStatus.ACTIVE));

        UUID contactId = UUID.randomUUID();
        organisation.addContact(new Contact(contactId, "Jane Doe", "jane@thinklab.com", "+5511999999999", ContactRole.TECHNICAL));

        organisation.activate();

        // 1. Domain -> Document
        OrganisationDocument doc = OrganisationPersistenceMapper.toDocument(organisation);

        assertNotNull(doc);
        assertEquals(organisationId, doc.getId());
        assertEquals("ThinkLab Corp", doc.getCorporateName());
        assertEquals("ThinkLab", doc.getTradeName());
        assertEquals("12345678000190", doc.getTaxIdentifier());
        assertEquals("ACTIVE", doc.getStatus());
        assertNotNull(doc.getBilling());
        assertEquals("finance@thinklab.com", doc.getBilling().billingEmail());
        assertEquals(1, doc.getOrganisationUnits().size());
        assertEquals("Headquarters", doc.getOrganisationUnits().get(0).unitName());
        assertEquals("ACTIVE", doc.getOrganisationUnits().get(0).status());
        assertEquals(1, doc.getContacts().size());
        assertEquals("Jane Doe", doc.getContacts().get(0).fullName());

        // 2. Document -> Domain
        Organisation mappedDomain = OrganisationPersistenceMapper.toDomain(doc);

        assertNotNull(mappedDomain);
        assertEquals(organisationId, mappedDomain.getId());
        assertEquals("ThinkLab Corp", mappedDomain.getCorporateName());
        assertEquals("ThinkLab", mappedDomain.getTradeName());
        assertEquals("12345678000190", mappedDomain.getTaxIdentifier());
        assertEquals(OrganisationStatus.ACTIVE, mappedDomain.getStatus());
        assertNotNull(mappedDomain.getBilling());
        assertEquals("finance@thinklab.com", mappedDomain.getBilling().billingEmail());
        assertEquals(1, mappedDomain.getOrganisationUnits().size());
        assertEquals("Headquarters", mappedDomain.getOrganisationUnits().get(0).unitName());
        assertEquals(OrganisationUnit.OrganisationUnitStatus.ACTIVE, mappedDomain.getOrganisationUnits().get(0).status());
        assertEquals(1, mappedDomain.getContacts().size());
        assertEquals("Jane Doe", mappedDomain.getContacts().get(0).fullName());
    }
}
