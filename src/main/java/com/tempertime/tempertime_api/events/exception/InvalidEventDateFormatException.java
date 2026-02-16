package com.tempertime.tempertime_api.events.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class InvalidEventDateFormatException extends RuntimeException {

    public InvalidEventDateFormatException() {
        super(ErrorCode.INVALID_EVENT_DATE_FORMAT.getDefaultMessage());
    }
}
