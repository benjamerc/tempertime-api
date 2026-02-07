package com.tempertime.tempertime_api.events.exception;

public class EventAccessDeniedException extends RuntimeException {
    public EventAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventAccessDeniedException(String message) {
        super(message);
    }
}
