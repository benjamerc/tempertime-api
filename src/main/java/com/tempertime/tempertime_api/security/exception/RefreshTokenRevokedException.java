package com.tempertime.tempertime_api.security.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

/**
 * Thrown when a refresh token has been revoked.
 */
public class RefreshTokenRevokedException extends RuntimeException {

    public RefreshTokenRevokedException() {
        super(ErrorCode.REFRESH_TOKEN_REVOKED.getDefaultMessage());
    }
}
