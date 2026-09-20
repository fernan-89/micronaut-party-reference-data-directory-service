package com.thinklab.application.mapper;

import com.thinklab.application.dto.response.OrganisationResponse;
import com.thinklab.domain.model.Organisation;
import com.thinklab.domain.model.Organisation.Billing;
import com.thinklab.domain.model.Organisation.Contact;
import com.thinklab.domain.model.Organisation.ContactRole;
import com.thinklab.domain.model.Organisation.OrganisationStatus;
import com.thinklab.domain.model.Organisation.OrganisationUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrganisationMapperTest {

    @Test
    @DisplayName("Should map full Organisation aggregate to OrganisationResponse DTO")
    void testToResponseWithAllFields() {
        UUID organisationId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();
        Instant now = Instant.now();

        OrganisationUnit unit = new OrganisationUnit(unitId, "Main Unit", "123 Tech Ave", "San Francisco", "USA", "94105", OrganisationUnit.OrganisationUnitStatus.ACTIVE);
        Contact contact = new Contact(contactId, "Jane Doe", "jane@thinklab.com", "+1-555-0100", ContactRole.ADMIN);
        Billing billing = new Billing("finance@thinklab.com", "USD", "STANDARD");

        Organisation organisation = Organisation.reconstitute(
                organisationId,
                "Thinklab Technologies Inc",
                "Thinklab",
                "12.345.678/0001-99",
                OrganisationStatus.ACTIVE,
                billing,
                List.of(unit),
                List.of(contact),
                now,
                now
        );

        OrganisationResponse response = OrganisationMapper.toResponse(organisation);

        assertNotNull(response);
        assertEquals(organisationId, response.id());
        assertEquals("Thinklab Technologies Inc", response.corporateName());
        assertEquals("Thinklab", response.tradeName());
        assertEquals("12.345.678/0001-99", response.taxIdentifier());
        assertEquals("ACTIVE", response.status());
        assertEquals(now, response.createdAt());
        assertEquals(now, response.updatedAt());

        // Verify OrganisationUnits
        assertNotNull(response.organisationUnits());
        assertEquals(1, response.organisationUnits().size());
        OrganisationResponse.OrganisationUnitResponse unitResponse = response.organisationUnits().get(0);
        assertEquals(unitId, unitResponse.unitId());
        assertEquals("Main Unit", unitResponse.unitName());
        assertEquals("123 Tech Ave", unitResponse.address());
        assertEquals("San Francisco", unitResponse.city());
        assertEquals("USA", unitResponse.country());
        assertEquals("94105", unitResponse.zipCode());
        assertEquals("ACTIVE", unitResponse.status());

        // Verify Contacts
        assertNotNull(response.contacts());
        assertEquals(1, response.contacts().size());
        OrganisationResponse.ContactResponse contactResponse = response.contacts().get(0);
        assertEquals(contactId, contactResponse.contactId());
        assertEquals("Jane Doe", contactResponse.fullName());
        assertEquals("jane@thinklab.com", contactResponse.email());
        assertEquals("+1-555-0100", contactResponse.phoneNumber());
        assertEquals("ADMIN", contactResponse.role());

        // Verify Billing
        assertNotNull(response.billing());
        assertEquals("finance@thinklab.com", response.billing().billingEmail());
        assertEquals("USD", response.billing().currency());
        assertEquals("STANDARD", response.billing().taxRegime());
    }

    @Test
    @DisplayName("Should handle null and empty lists gracefully in OrganisationMapper")
    void testToResponseWithNullsAndEmptyLists() {
        UUID organisationId = UUID.randomUUID();
        Instant now = Instant.now();

        Organisation organisation = Organisation.reconstitute(
                organisationId,
                "Minimal Organisation",
                "Minimal",
                "00.000.000/0001-00",
                OrganisationStatus.PENDING_ACTIVATION,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                now,
                now
        );

        OrganisationResponse response = OrganisationMapper.toResponse(organisation);

        assertNotNull(response);
        assertEquals(organisationId, response.id());
        assertEquals(0, response.organisationUnits().size());
        assertEquals(0, response.contacts().size());
        assertNull(response.billing());
    }
}
