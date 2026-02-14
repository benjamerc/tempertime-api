package com.tempertime.tempertime_api.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown when a JWT access token has expired.
 */
public class AccessTokenExpiredException extends AuthenticationException {

    public AccessTokenExpiredException(Throwable cause) {
        super("Access token expired", cause);
    }
}
