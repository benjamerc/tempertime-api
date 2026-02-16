package com.tempertime.tempertime_api.security.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

/**
 * Thrown when a refresh token has expired.
 */
public class RefreshTokenExpiredException extends RuntimeException {

    public RefreshTokenExpiredException() {
        super(ErrorCode.REFRESH_TOKEN_EXPIRED.getDefaultMessage());
    }
}
