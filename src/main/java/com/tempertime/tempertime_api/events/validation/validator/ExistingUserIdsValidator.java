package com.tempertime.tempertime_api.events.validation.validator;

import com.tempertime.tempertime_api.events.validation.annotation.ExistingUserIds;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validator that checks existence of user IDs using the repository.
 */
@Component
@RequiredArgsConstructor
public class ExistingUserIdsValidator
        implements ConstraintValidator<ExistingUserIds, List<Long>> {

    private final UserRepository userRepository;

    @Override
    public boolean isValid(List<Long> userIds, ConstraintValidatorContext context) {

        if (userIds == null || userIds.isEmpty()) {
            return true;
        }

        long existingCount = userRepository.countByIdIn(userIds);
        return existingCount == userIds.size();
    }
}
