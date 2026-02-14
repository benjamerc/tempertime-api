package com.tempertime.tempertime_api.security.core;

import com.tempertime.tempertime_api.users.domain.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;

/**
 * Provides information about the currently authenticated user.
 */
@Component
public class CurrentUserProvider {

    public Long getUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userDetails.getUser().getId();
    }

    public User getUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userDetails.getUser();
    }
}
