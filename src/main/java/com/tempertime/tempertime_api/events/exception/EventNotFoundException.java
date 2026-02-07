package com.tempertime.tempertime_api.events.exception;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventNotFoundException(String message) {
        super(message);
    }
}
