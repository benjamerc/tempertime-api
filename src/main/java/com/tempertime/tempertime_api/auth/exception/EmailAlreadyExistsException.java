package com.tempertime.tempertime_api.auth.exception;

/**
 * Thrown when attempting to register a user with an email
 * that is already registered in the system.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException() {
        super("Email already registered");
    }
}
