package com.tempertime.tempertime_api.users.service;

import com.tempertime.tempertime_api.users.dto.request.UserDeleteAccountRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdatePasswordRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdateProfileRequest;
import com.tempertime.tempertime_api.users.dto.response.UserProfileResponse;

/** Authenticated user profile management */
public interface UserService {

    UserProfileResponse getProfile(Long userId);

    UserProfileResponse updateProfile(Long userId, UserUpdateProfileRequest request);

    void updatePassword(Long userId, UserUpdatePasswordRequest request);

    void deleteAccount(Long userId, UserDeleteAccountRequest request);
}
