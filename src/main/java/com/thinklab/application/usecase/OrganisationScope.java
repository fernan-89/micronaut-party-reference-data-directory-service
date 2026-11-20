package com.thinklab.application.usecase;

import com.thinklab.domain.exception.OrganisationNotFoundException;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * The tenant of a call is an Organisation, and an Organisation is the tenant (ADR-016): a route that takes an organisation id answers only when that id is the
 * caller's own X-Tenant-Id. Another organisation is the same as none, so a UUID is not enough to read or change someone else's data.
 */
final class OrganisationScope {

    private OrganisationScope() {
    }

    static Mono<Void> own(UUID organisationId, UUID tenantId) {
        return organisationId.equals(tenantId) ? Mono.empty() : Mono.error(new OrganisationNotFoundException(organisationId));
    }
}
