package com.tempertime.tempertime_api.events.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class EventDateLimitExceededException extends RuntimeException {

    public EventDateLimitExceededException() {
        super(ErrorCode.EVENT_DATE_LIMIT_EXCEEDED.getDefaultMessage());
    }
}
