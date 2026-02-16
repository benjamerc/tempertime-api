package com.tempertime.tempertime_api.events.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class InvalidEventDateValueException extends RuntimeException {

    public InvalidEventDateValueException() {
        super(ErrorCode.INVALID_EVENT_DATE_VALUE.getDefaultMessage());
    }
}
