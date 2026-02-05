package com.tempertime.tempertime_api.common.color;

public class InvalidColorFormatException extends RuntimeException {
    public InvalidColorFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidColorFormatException(String message) {
        super(message);
    }
}
