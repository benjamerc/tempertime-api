package com.tempertime.tempertime_api.security.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;
import org.springframework.security.core.AuthenticationException;

/**
 * Thrown when a JWT access token has expired.
 */
public class AccessTokenExpiredException extends AuthenticationException {

    public AccessTokenExpiredException(Throwable cause) {
        super(ErrorCode.ACCESS_TOKEN_EXPIRED.getDefaultMessage(), cause);
    }
}
