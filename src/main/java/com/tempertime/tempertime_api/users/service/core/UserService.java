package com.tempertime.tempertime_api.users.service.core;

import com.tempertime.tempertime_api.users.dto.request.UserDeleteAccountRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdatePasswordRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdateProfileRequest;
import com.tempertime.tempertime_api.users.dto.response.UserProfileResponse;

/**
 * Authenticated user profile management.
 */
public interface UserService {

    /**
     * Retrieves the profile of the user with the given ID.
     */
    UserProfileResponse getProfile(Long userId);

    /**
     * Updates the profile fields of the user with the given ID.
     */
    UserProfileResponse updateProfile(Long userId, UserUpdateProfileRequest request);

    /**
     * Updates the password of the user with the given ID.
     * Requires the current password to be correct.
     */
    void updatePassword(Long userId, UserUpdatePasswordRequest request);

    /**
     * Deletes the account of the user with the given ID.
     * Requires the current password to be correct.
     */
    void deleteAccount(Long userId, UserDeleteAccountRequest request);
}
