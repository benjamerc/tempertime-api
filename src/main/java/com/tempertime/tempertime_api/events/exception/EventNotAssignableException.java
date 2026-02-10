package com.tempertime.tempertime_api.events.exception;

public class EventNotAssignableException extends RuntimeException {
    public EventNotAssignableException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventNotAssignableException(String message) {
        super(message);
    }
}
