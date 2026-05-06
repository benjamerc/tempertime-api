package com.tempertime.tempertime_api.users;

import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.dto.response.UserProfileResponse;

import java.time.Instant;
import java.util.UUID;

public class UserTestDataProvider {

    public static final String FIRST_NAME = "Test";
    public static final String LAST_NAME = "User";
    public static final String PASSWORD = "Password123";
    public static final String PASSWORD_HASH = "hashedPassword";

    public static String email() {
        return "test_" + UUID.randomUUID() + "@mail.com";
    }

    public static User user(Long id) {
        return User.builder()
                .id(id)
                .email(email())
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .passwordHash(PASSWORD_HASH)
                .createdAt(Instant.now())
                .build();
    }

    public static UserProfileResponse userProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt()
        );
    }
}
