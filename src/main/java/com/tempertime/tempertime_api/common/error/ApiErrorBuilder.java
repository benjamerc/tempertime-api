package com.tempertime.tempertime_api.common.error;

import com.tempertime.tempertime_api.common.error.model.ApiError;
import com.tempertime.tempertime_api.common.error.model.ErrorCode;
import com.tempertime.tempertime_api.common.error.model.FieldError;
import org.springframework.stereotype.Component;

import java.util.List;

/** Builds ApiError instances for API responses. */
@Component
public class ApiErrorBuilder {

    /** Builds an ApiError without field-level details. */
    public ApiError build(
            ErrorCode code,
            String message,
            String path
    ) {
        return ApiError.builder()
                .code(code)
                .message(message)
                .path(path)
                .build();
    }

    /** Builds an ApiError including field-level validation details */
    public ApiError build(
            ErrorCode code,
            String message,
            String path,
            List<FieldError> details
    ) {
        return ApiError.builder()
                .code(code)
                .message(message)
                .path(path)
                .details(details)
                .build();
    }
}
