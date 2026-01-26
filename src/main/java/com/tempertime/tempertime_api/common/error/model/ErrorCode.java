package com.tempertime.tempertime_api.common.error.model;

/** API error codes grouped by origin */
public enum ErrorCode {

    // Auth
    BAD_CREDENTIALS,
    EMAIL_ALREADY_EXISTS,

    // Security
    REFRESH_TOKEN_EXPIRED,
    REFRESH_TOKEN_NOT_FOUND,
    REFRESH_TOKEN_REVOKED,
    EMAIL_NOT_FOUND,

    // Common
    VALIDATION_ERROR,
    INTERNAL_ERROR
}
