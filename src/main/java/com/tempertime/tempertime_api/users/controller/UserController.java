package com.tempertime.tempertime_api.users.controller;

import com.tempertime.tempertime_api.security.core.CurrentUserProvider;
import com.tempertime.tempertime_api.users.dto.request.UserDeleteAccountRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdatePasswordRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdateProfileRequest;
import com.tempertime.tempertime_api.users.dto.response.UserProfileResponse;
import com.tempertime.tempertime_api.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileResponse> userProfile() {

        return ResponseEntity.ok(
                userService.getProfile(
                        currentUserProvider.getUserId()
                )
        );
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UserUpdateProfileRequest request
    ) {

        return ResponseEntity.ok(
                userService.updateProfile(
                        currentUserProvider.getUserId(),
                        request
                )
        );
    }

    @PatchMapping("/me/password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> updatePassword(
            @Valid @RequestBody UserUpdatePasswordRequest request
    ) {

        userService.updatePassword(
                currentUserProvider.getUserId(),
                request
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteAccount(
            @Valid @RequestBody UserDeleteAccountRequest request
    ) {

        userService.deleteAccount(
                currentUserProvider.getUserId(),
                request
        );

        return ResponseEntity.noContent().build();
    }
}
