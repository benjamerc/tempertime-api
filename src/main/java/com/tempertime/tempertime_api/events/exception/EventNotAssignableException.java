package com.tempertime.tempertime_api.events.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class EventNotAssignableException extends RuntimeException {

    public EventNotAssignableException() {
        super(ErrorCode.EVENT_NOT_ASSIGNABLE.getDefaultMessage());
    }
}
