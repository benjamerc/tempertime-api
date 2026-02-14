package com.tempertime.tempertime_api.security.core;

import com.tempertime.tempertime_api.users.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Provides information about the currently authenticated user.
 */
@Component
public class CurrentUserProvider {

    public Long getUserId() {
        return getCurrentUser().getId();
    }

    public User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new IllegalStateException("No authenticated user found");
        }

        return userDetails.getUser();
    }
}

