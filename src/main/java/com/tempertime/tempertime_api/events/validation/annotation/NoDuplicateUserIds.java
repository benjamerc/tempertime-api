package com.tempertime.tempertime_api.events.validation.annotation;

import com.tempertime.tempertime_api.events.validation.validator.NoDuplicateUserIdsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/** Validates that the list of user IDs does not contain duplicates */
@Documented
@Constraint(validatedBy = NoDuplicateUserIdsValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NoDuplicateUserIds {

    String message() default "User IDs must be unique";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
