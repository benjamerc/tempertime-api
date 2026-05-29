package com.tempertime.tempertime_api.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response containing access and refresh tokens
 * returned after successful authentication or refresh.
 */
@Schema(
        description = "Response containing authentication tokens"
)
public record AuthTokenResponse(

        @Schema(
                description = "JWT access token used to authenticate requests",
                example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlkIjoxLCJyb2xlIjoiVVNFUiIsImlhdCI6MTc3OTc1NjE2MiwiZXhwIjoxNzc5NzU3OTYyfQ.o247JfnTY55OvQe4u4_UXdffrGjAcm1-sBkUdhR8FnzPGnju5pXv3CRTTc4CrVnw0ac-2dt4GakfPxHCYOybDA"
        )
        String accessToken,

        @Schema(
                description = "Refresh token used to generate new access tokens",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        String refreshToken
) {}
