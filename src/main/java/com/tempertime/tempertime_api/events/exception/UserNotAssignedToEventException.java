package com.tempertime.tempertime_api.events.exception;

public class UserNotAssignedToEventException extends RuntimeException {
    public UserNotAssignedToEventException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotAssignedToEventException(String message) {
        super(message);
    }
}
