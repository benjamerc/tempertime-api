package com.tempertime.tempertime_api.security.refresh;

import com.tempertime.tempertime_api.users.model.User;

/** Refresh token lifecycle operations */
public interface RefreshTokenService {

    String createRefreshToken(User user);

    RefreshToken validateRefreshToken(String rawToken);

    String rotateRefreshToken(RefreshToken refreshToken);

    void revokeRefreshToken(String rawToken);

    void revokeAllRefreshTokensForUser(User user);
}
