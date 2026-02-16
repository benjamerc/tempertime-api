package com.tempertime.tempertime_api.events.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException() {
        super(ErrorCode.EVENT_NOT_FOUND.getDefaultMessage());
    }
}
