package com.tempertime.tempertime_api.users.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

/**
 * Thrown when the user's current password is incorrect.
 */
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD.getDefaultMessage());
    }
}
