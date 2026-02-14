package com.tempertime.tempertime_api.security.jwt;

import com.tempertime.tempertime_api.users.domain.User;
import io.jsonwebtoken.Claims;

/**
 * Defines operations for creating and validating JWT access tokens.
 */
public interface AccessTokenService {

    /**
     * Creates a new JWT access token for the given user.
     */
    String createAccessToken(User user);

    /**
     * Validates the provided JWT access token and returns its claims.
     */
    Claims validateAccessToken(String token);
}
