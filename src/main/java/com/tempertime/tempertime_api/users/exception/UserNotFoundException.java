package com.tempertime.tempertime_api.users.exception;

/**
 * Thrown when the user is not found in the database.
 * Used in user loaders, not to be confused with UsernameNotFoundException.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
