package com.thinklab.domain.exception;

/**
 * Domain Exception: Thrown when an attempt is made to create an Organisation that already
 * exists for a given tax identifier.
 *
 * <p>RFC 7807 mapping: HTTP 409 Conflict.
 *
 * @author ThinkLab
 * @since 1.0
 */
public class DuplicateOrganisationException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "ERR-ORG-00409";

    public DuplicateOrganisationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public DuplicateOrganisationException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }
}
