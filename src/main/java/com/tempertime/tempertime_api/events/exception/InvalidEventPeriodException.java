package com.tempertime.tempertime_api.events.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class InvalidEventPeriodException extends RuntimeException {

    public InvalidEventPeriodException() {
        super(ErrorCode.INVALID_EVENT_PERIOD.getDefaultMessage());
    }

    public InvalidEventPeriodException(String message) {
        super(message);
    }
}
