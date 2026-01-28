package com.tempertime.tempertime_api.security.exception;

import org.springframework.security.core.AuthenticationException;

public class AccessTokenExpiredException extends AuthenticationException {
    public AccessTokenExpiredException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public AccessTokenExpiredException(String message) {
        super(message);
    }
}
