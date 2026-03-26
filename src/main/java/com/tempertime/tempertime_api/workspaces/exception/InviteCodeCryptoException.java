package com.tempertime.tempertime_api.workspaces.exception;

/**
 * Thrown when a cryptographic operation (encrypt/decrypt) fails due to system error.
 */
public class InviteCodeCryptoException extends RuntimeException {

    public InviteCodeCryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
