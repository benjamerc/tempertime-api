package com.tempertime.tempertime_api.security.exception;

/**
 * Thrown when a refresh token has been revoked.
 */
public class RefreshTokenRevokedException extends RuntimeException {

    public RefreshTokenRevokedException() {
        super("Refresh token revoked");
    }
}
