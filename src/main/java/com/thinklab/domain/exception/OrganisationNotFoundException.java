package com.thinklab.domain.exception;

import java.util.Objects;
import java.util.UUID;

/**
 * Domain Exception: Indicates that a requested {@link com.thinklab.domain.model.Organisation}
 * (or subordinate OrganisationUnit) could not be resolved from the repository.
 *
 * <p>RFC 7807 mapping: HTTP 404 Not Found.
 *
 * @author ThinkLab
 * @since 1.0
 */
public class OrganisationNotFoundException extends BusinessException {

    private static final String ERROR_CODE = "ERR-ORG-00404";

    public OrganisationNotFoundException(UUID id) {
        super(
                ERROR_CODE,
                String.format("Organisation with sovereign ID [%s] could not be found in the system of record.",
                        Objects.requireNonNull(id, "Domain Exception constraint violated: UUID cannot be null."))
        );
    }

    public OrganisationNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}
