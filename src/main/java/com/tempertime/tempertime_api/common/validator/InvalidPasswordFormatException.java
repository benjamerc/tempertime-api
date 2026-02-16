package com.tempertime.tempertime_api.common.validator;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class InvalidPasswordFormatException extends RuntimeException {

    public InvalidPasswordFormatException() {
        super(ErrorCode.INVALID_PASSWORD_FORMAT.getDefaultMessage());
    }
}
