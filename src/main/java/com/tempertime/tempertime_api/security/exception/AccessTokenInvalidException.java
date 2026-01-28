package com.tempertime.tempertime_api.security.exception;

import org.springframework.security.core.AuthenticationException;

public class AccessTokenInvalidException extends AuthenticationException {
    public AccessTokenInvalidException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public AccessTokenInvalidException(String message) {
        super(message);
    }
}
