package com.tempertime.tempertime_api.auth.dto.response;

/** Auth token response used for login and token refresh */
public record AuthTokenResponse(

        String accessToken,

        String refreshToken
) {}
