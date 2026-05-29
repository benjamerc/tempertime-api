package com.tempertime.tempertime_api.common.error.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Represents a single field validation error from @Valid requests.
 */
@Schema(
        description = "Single field validation error"
)
@Getter
@AllArgsConstructor
@Builder
public class FieldError {

    @Schema(
            description = "Name of the field that failed validation"
    )
    private String field;

    @Schema(
            description = "Human-readable validation error message"
    )
    private String message;
}
