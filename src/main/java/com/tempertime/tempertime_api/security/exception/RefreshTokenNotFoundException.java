package com.tempertime.tempertime_api.security.exception;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RefreshTokenNotFoundException(String message) {
        super(message);
    }
}
