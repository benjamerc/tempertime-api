package com.tempertime.tempertime_api.security.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;
import org.springframework.security.core.AuthenticationException;

/**
 * Thrown when a JWT access token is invalid or malformed.
 */
public class AccessTokenInvalidException extends AuthenticationException {

    public AccessTokenInvalidException(Throwable cause) {
        super(ErrorCode.ACCESS_TOKEN_INVALID.getDefaultMessage(), cause);
    }
}
