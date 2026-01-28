package com.tempertime.tempertime_api.users.controller;

import com.tempertime.tempertime_api.security.core.CustomUserDetails;
import com.tempertime.tempertime_api.users.dto.request.UserDeleteAccountRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdatePasswordRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdateProfileRequest;
import com.tempertime.tempertime_api.users.dto.response.UserProfileResponse;
import com.tempertime.tempertime_api.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileResponse> userProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(userService.getProfile(userDetails.getUser().getId()));
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                             @Valid @RequestBody UserUpdateProfileRequest request) {

        return ResponseEntity.ok(userService.updateProfile(userDetails.getUser().getId(), request));
    }

    @PatchMapping("/me/password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @Valid @RequestBody UserUpdatePasswordRequest request) {

        userService.updatePassword(userDetails.getUser().getId(), request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @Valid @RequestBody UserDeleteAccountRequest request) {

        userService.deleteAccount(userDetails.getUser().getId(), request);

        return ResponseEntity.noContent().build();
    }
}
