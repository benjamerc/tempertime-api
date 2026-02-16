package com.tempertime.tempertime_api.events.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class UserNotAssignedToEventException extends RuntimeException {

    public UserNotAssignedToEventException() {
        super(ErrorCode.USER_NOT_ASSIGNED_TO_EVENT.getDefaultMessage());
    }
}
