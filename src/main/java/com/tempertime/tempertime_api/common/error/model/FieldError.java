package com.tempertime.tempertime_api.common.error.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Represents a single field validation error */
@Getter
@AllArgsConstructor
@Builder
public class FieldError {

    private String field;
    private String message;
}
