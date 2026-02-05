package com.tempertime.tempertime_api.events.exception;

public class InvalidEventDateFormatException extends RuntimeException {
    public InvalidEventDateFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidEventDateFormatException(String message) {
        super(message);
    }
}
