package com.tempertime.tempertime_api.users.service.core;

import com.tempertime.tempertime_api.security.refresh.RefreshTokenService;
import com.tempertime.tempertime_api.users.dto.request.UserDeleteAccountRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdatePasswordRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdateProfileRequest;
import com.tempertime.tempertime_api.users.dto.response.UserProfileResponse;
import com.tempertime.tempertime_api.users.exception.InvalidPasswordException;
import com.tempertime.tempertime_api.users.mapper.UserMapper;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import com.tempertime.tempertime_api.users.service.loader.UserLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final UserLoader userLoader;

    @Override
    public UserProfileResponse getProfile(Long userId) {

        User user = userLoader.loadUserOrThrow(userId);

        return userMapper.toUserProfileResponse(user);
    }

    /**
     * Updates the user's profile.
     * Only fields provided in the request are updated.
     */
    @Override
    public UserProfileResponse updateProfile(Long userId, UserUpdateProfileRequest request) {

        User user = userLoader.loadUserOrThrow(userId);

        Optional.ofNullable(request.firstName())
                .filter(f -> !f.isBlank())
                .ifPresent(user::setFirstName);

        Optional.ofNullable(request.lastName())
                .filter(l -> !l.isBlank())
                .ifPresent(user::setLastName);

        return userMapper.toUserProfileResponse(userRepository.save(user));
    }

    /**
     * Updates user's password.
     * Requires the current password to be correct.
     * Revokes all active refresh tokens after the update.
     */
    @Override
    public void updatePassword(Long userId, UserUpdatePasswordRequest request) {

        User user = userLoader.loadUserOrThrow(userId);

        validateCurrentPassword(user, request.currentPassword());

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        refreshTokenService.revokeAllRefreshTokensForUser(user);
    }

    /**
     * Deletes user's account.
     * Requires the current password to be correct.
     * All associated refresh tokens are removed via cascade.
     */
    @Transactional
    @Override
    public void deleteAccount(Long userId, UserDeleteAccountRequest request) {

        User user = userLoader.loadUserOrThrow(userId);

        validateCurrentPassword(user, request.currentPassword());

        userRepository.delete(user);
    }

    private void validateCurrentPassword(User user, String currentPassword) {

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidPasswordException();
        }
    }
}
