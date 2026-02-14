package com.tempertime.tempertime_api.common.error.model;

import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard API error response.
 */
@Getter
@AllArgsConstructor
@Builder
public class ApiError {

    private ErrorCode code;
    private String message;

    private String path;

    @Builder.Default
    private Instant timestamp = Instant.now();

    @Builder.Default
    private List<FieldError> details = new ArrayList<>();
}
