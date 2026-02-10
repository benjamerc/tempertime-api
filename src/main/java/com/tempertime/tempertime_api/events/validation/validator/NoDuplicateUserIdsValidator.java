package com.tempertime.tempertime_api.events.validation.validator;

import com.tempertime.tempertime_api.events.validation.annotation.NoDuplicateUserIds;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Validator that ensures all user IDs in the list are unique */
public class NoDuplicateUserIdsValidator
        implements ConstraintValidator<NoDuplicateUserIds, List<Long>> {

    @Override
    public boolean isValid(List<Long> userIds, ConstraintValidatorContext context) {

        if (userIds == null || userIds.isEmpty()) {
            return true;
        }

        Set<Long> uniqueIds = new HashSet<>(userIds);
        return uniqueIds.size() == userIds.size();
    }
}
