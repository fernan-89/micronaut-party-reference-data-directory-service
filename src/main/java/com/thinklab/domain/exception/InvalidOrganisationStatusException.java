package com.thinklab.domain.exception;

/**
 * Domain Exception: Indicates an illegal or unpermitted lifecycle state transition attempt
 * on an {@link com.thinklab.domain.model.Organisation} or its subordinate OrganisationUnit.
 *
 * <p>RFC 7807 mapping: HTTP 409 Conflict.
 *
 * @author ThinkLab
 * @since 1.0
 */
public class InvalidOrganisationStatusException extends BusinessException {

    private static final String ERROR_CODE = "ERR-ORG-00409";

    public InvalidOrganisationStatusException(String message) {
        super(ERROR_CODE, message);
    }
}
