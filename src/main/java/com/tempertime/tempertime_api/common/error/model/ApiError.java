package com.tempertime.tempertime_api.common.error.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard API error response.
 */
@Schema(
        description = "Standard error response returned by the API when a request fails"
)
@Getter
@AllArgsConstructor
@Builder
public class ApiError {

    @Schema(
            description = "Machine-readable error code representing the failure type"
    )
    private ErrorCode code;

    @Schema(
            description = "Human-readable description of the error"
    )
    private String message;

    @Schema(
            description = "API request path where the error occurred",
            nullable = true
    )
    private String path;

    @Schema(
            description = "Timestamp when the error occurred (ISO 8601 UTC)"
    )
    @Builder.Default
    private Instant timestamp = Instant.now();

    @Schema(
            description = "List of field validation errors (only present in VALIDATION_ERROR failures)",
            nullable = true
    )
    @Builder.Default
    private List<FieldError> details = new ArrayList<>();
}
