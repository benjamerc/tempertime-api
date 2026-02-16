package com.tempertime.tempertime_api.events.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class EventAccessDeniedException extends RuntimeException {

    public EventAccessDeniedException() {
        super(ErrorCode.EVENT_ACCESS_DENIED.getDefaultMessage());
    }
}
