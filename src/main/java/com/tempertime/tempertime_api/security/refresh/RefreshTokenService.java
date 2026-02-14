package com.tempertime.tempertime_api.security.refresh;

import com.tempertime.tempertime_api.users.domain.User;

public interface RefreshTokenService {

    /**
     * Creates a new refresh token for the given user.
     */
    String createRefreshToken(User user);

    /**
     * Validates a refresh token and returns its entity.
     */
    RefreshToken validateRefreshToken(String rawToken);

    /**
     * Rotates a refresh token by revoking the old one and issuing a new token.
     */
    String rotateRefreshToken(RefreshToken refreshToken);

    /**
     * Revokes the specified refresh token.
     */
    void revokeRefreshToken(String rawToken);

    /**
     * Revokes all refresh tokens for a given user.
     */
    void revokeAllRefreshTokensForUser(User user);
}
