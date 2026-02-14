package com.tempertime.tempertime_api.common.hash;

/**
 * Thrown when an error occurs during hashing operations.
 */
public class HashingException extends RuntimeException {

    public HashingException(String message, Throwable cause) {
        super(message, cause);
    }
}
