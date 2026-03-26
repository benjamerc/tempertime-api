package com.tempertime.tempertime_api.workspaces.exception;

/**
 * Thrown when the encrypted invite code is invalid or has been tampered with.
 */
public class InvalidEncryptedInviteCodeException extends RuntimeException {

    public InvalidEncryptedInviteCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
