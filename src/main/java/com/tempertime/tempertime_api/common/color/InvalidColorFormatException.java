package com.tempertime.tempertime_api.common.color;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class InvalidColorFormatException extends RuntimeException {

    public InvalidColorFormatException() {
        super(ErrorCode.INVALID_COLOR_FORMAT.getDefaultMessage());
    }
}
