package com.tempertime.tempertime_api.security.exception;

/**
 * Thrown when a refresh token cannot be found in the database.
 */
public class RefreshTokenNotFoundException extends RuntimeException {

    public RefreshTokenNotFoundException() {
        super("Refresh token not found");
    }
}
