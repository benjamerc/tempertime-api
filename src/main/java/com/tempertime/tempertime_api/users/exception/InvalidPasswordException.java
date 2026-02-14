package com.tempertime.tempertime_api.users.exception;

/**
 * Thrown when the user's current password is incorrect.
 */
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}
