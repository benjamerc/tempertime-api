package com.tempertime.tempertime_api.auth.service;

import com.tempertime.tempertime_api.auth.dto.request.AuthLoginRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRefreshTokenRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRegisterRequest;
import com.tempertime.tempertime_api.auth.dto.response.AuthRegisterResponse;
import com.tempertime.tempertime_api.auth.dto.response.AuthTokenResponse;

/**
 * Defines authentication operations including user registration,
 * login and refresh token management.
 *
 * Handles issuance and invalidation of authentication tokens.
 */
public interface AuthService {

    /**
     * Registers a new user in the system.
     */
    AuthRegisterResponse register(AuthRegisterRequest request);

    /**
     * Authenticates a user and issues new access and refresh tokens.
     */
    AuthTokenResponse login(AuthLoginRequest request);

    /**
     * Issues new access and refresh tokens using a valid refresh token.
     */
    AuthTokenResponse refresh(AuthRefreshTokenRequest request);

    /**
     * Invalidates the provided refresh token.
     */
    void logout(AuthRefreshTokenRequest request);
}
