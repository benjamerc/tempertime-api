package com.tempertime.tempertime_api.auth.dto.response;

/**
 * Response containing access and refresh tokens
 * returned after successful authentication or refresh.
 */
public record AuthTokenResponse(

        String accessToken,

        String refreshToken
) {}
