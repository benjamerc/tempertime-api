package com.tempertime.tempertime_api.common.error.mapper;

import com.tempertime.tempertime_api.common.error.model.FieldError;
import lombok.experimental.UtilityClass;
import org.springframework.validation.BindingResult;

import java.util.List;

/**
 * Maps BindingResult to API FieldError object.
 */
@UtilityClass
public class FieldErrorMapper {

    public List<FieldError> from(BindingResult result) {
        return result.getFieldErrors().stream()
                .map(fe -> FieldError.builder()
                        .field(fe.getField())
                        .message(fe.getDefaultMessage())
                        .build())
                .toList();
    }
}
