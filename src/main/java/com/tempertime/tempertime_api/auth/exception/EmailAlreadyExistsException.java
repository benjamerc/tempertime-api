package com.tempertime.tempertime_api.auth.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

/**
 * Thrown when attempting to register a user with an email
 * that is already registered in the system.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException() {
        super(ErrorCode.EMAIL_ALREADY_EXISTS.getDefaultMessage());
    }
}
