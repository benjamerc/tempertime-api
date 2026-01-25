package com.tempertime.tempertime_api.auth.service;

import com.tempertime.tempertime_api.auth.dto.request.AuthLoginRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRefreshTokenRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRegisterRequest;
import com.tempertime.tempertime_api.auth.dto.response.AuthRegisterResponse;
import com.tempertime.tempertime_api.auth.dto.response.AuthTokenResponse;

/** Authentication and token management use cases */
public interface AuthService {

    AuthRegisterResponse register(AuthRegisterRequest request);

    AuthTokenResponse login(AuthLoginRequest request);

    /** Issues new tokens from a valid refresh token */
    AuthTokenResponse refresh(AuthRefreshTokenRequest request);

    /** Invalidates the refresh token */
    void logout(AuthRefreshTokenRequest request);
}
