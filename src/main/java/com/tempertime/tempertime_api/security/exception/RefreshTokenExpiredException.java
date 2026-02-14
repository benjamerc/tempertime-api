package com.tempertime.tempertime_api.security.exception;

/**
 * Thrown when a refresh token has expired.
 */
public class RefreshTokenExpiredException extends RuntimeException {

    public RefreshTokenExpiredException() {
        super("Refresh token expired");
    }
}
