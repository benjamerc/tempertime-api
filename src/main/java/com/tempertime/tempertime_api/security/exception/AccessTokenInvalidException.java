package com.tempertime.tempertime_api.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown when a JWT access token is invalid or malformed.
 */
public class AccessTokenInvalidException extends AuthenticationException {

    public AccessTokenInvalidException(Throwable cause) {
        super("Access token invalid", cause);
    }
}
