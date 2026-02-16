package com.tempertime.tempertime_api.users.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

/**
 * Thrown when the user is not found in the database.
 * Used in user loaders, not to be confused with UsernameNotFoundException.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND.getDefaultMessage());
    }
}
