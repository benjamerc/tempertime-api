package com.tempertime.tempertime_api.users.dto.response;

import java.time.Instant;

/** Authenticated user profile response */
public record UserProfileResponse(

        String email,
        String firstName,
        String lastName,
        Instant createdAt
) {}
