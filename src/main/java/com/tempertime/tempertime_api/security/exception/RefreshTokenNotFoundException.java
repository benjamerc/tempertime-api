package com.tempertime.tempertime_api.security.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

/**
 * Thrown when a refresh token cannot be found in the database.
 */
public class RefreshTokenNotFoundException extends RuntimeException {

    public RefreshTokenNotFoundException() {
        super(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getDefaultMessage());
    }
}
