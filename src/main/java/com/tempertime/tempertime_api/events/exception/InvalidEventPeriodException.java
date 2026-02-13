package com.tempertime.tempertime_api.events.exception;

public class InvalidEventPeriodException extends RuntimeException {
    public InvalidEventPeriodException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidEventPeriodException(String message) {
        super(message);
    }
}
