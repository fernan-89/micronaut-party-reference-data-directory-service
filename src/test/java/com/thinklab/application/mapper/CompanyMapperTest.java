package com.thinklab.application.mapper;

import com.thinklab.application.dto.response.CompanyResponse;
import com.thinklab.domain.model.Company;
import com.thinklab.domain.model.Company.Billing;
import com.thinklab.domain.model.Company.Branch;
import com.thinklab.domain.model.Company.CompanyStatus;
import com.thinklab.domain.model.Company.Contact;
import com.thinklab.domain.model.Company.ContactRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CompanyMapperTest {

    @Test
    @DisplayName("Should map full Company aggregate to CompanyResponse DTO")
    void testToResponseWithAllFields() {
        UUID companyId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();
        Instant now = Instant.now();

        Branch branch = new Branch(branchId, "Main Branch", "123 Tech Ave", "San Francisco", "USA", "94105");
        Contact contact = new Contact(contactId, "Jane Doe", "jane@thinklab.com", "+1-555-0100", ContactRole.ADMIN);
        Billing billing = new Billing("finance@thinklab.com", "USD", "STANDARD");

        Company company = Company.reconstitute(
                companyId,
                "Thinklab Technologies Inc",
                "Thinklab",
                "12.345.678/0001-99",
                CompanyStatus.ACTIVE,
                billing,
                List.of(branch),
                List.of(contact),
                now,
                now
        );

        CompanyResponse response = CompanyMapper.toResponse(company);

        assertNotNull(response);
        assertEquals(companyId, response.id());
        assertEquals("Thinklab Technologies Inc", response.corporateName());
        assertEquals("Thinklab", response.tradeName());
        assertEquals("12.345.678/0001-99", response.taxIdentifier());
        assertEquals("ACTIVE", response.status());
        assertEquals(now, response.createdAt());
        assertEquals(now, response.updatedAt());

        // Verify Branches
        assertNotNull(response.branches());
        assertEquals(1, response.branches().size());
        CompanyResponse.BranchResponse branchResponse = response.branches().get(0);
        assertEquals(branchId, branchResponse.branchId());
        assertEquals("Main Branch", branchResponse.branchName());
        assertEquals("123 Tech Ave", branchResponse.address());
        assertEquals("San Francisco", branchResponse.city());
        assertEquals("USA", branchResponse.country());
        assertEquals("94105", branchResponse.zipCode());

        // Verify Contacts
        assertNotNull(response.contacts());
        assertEquals(1, response.contacts().size());
        CompanyResponse.ContactResponse contactResponse = response.contacts().get(0);
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
    @DisplayName("Should handle null and empty lists gracefully in CompanyMapper")
    void testToResponseWithNullsAndEmptyLists() {
        UUID companyId = UUID.randomUUID();
        Instant now = Instant.now();

        Company company = Company.reconstitute(
                companyId,
                "Minimal Company",
                "Minimal",
                "00.000.000/0001-00",
                CompanyStatus.PENDING_ACTIVATION,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                now,
                now
        );

        CompanyResponse response = CompanyMapper.toResponse(company);

        assertNotNull(response);
        assertEquals(companyId, response.id());
        assertEquals(0, response.branches().size());
        assertEquals(0, response.contacts().size());
        assertNull(response.billing());
    }
}
