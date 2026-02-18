package com.tempertime.tempertime_api.events.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class TimeZoneMissingException extends RuntimeException {

    public TimeZoneMissingException() {
        super(ErrorCode.INVALID_TIME_ZONE.getDefaultMessage());
    }
}
