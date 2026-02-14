package com.tempertime.tempertime_api.events.validation.annotation;

import com.tempertime.tempertime_api.events.validation.validator.ExistingUserIdsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that all provided user IDs exist in the system.
 */
@Documented
@Constraint(validatedBy = ExistingUserIdsValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistingUserIds {

    String message() default "One or more user IDs do not exist";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
