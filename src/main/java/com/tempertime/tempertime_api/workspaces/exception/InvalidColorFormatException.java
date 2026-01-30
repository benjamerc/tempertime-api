package com.tempertime.tempertime_api.workspaces.exception;

public class InvalidColorFormatException extends RuntimeException {
    public InvalidColorFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidColorFormatException(String message) {
        super(message);
    }
}
