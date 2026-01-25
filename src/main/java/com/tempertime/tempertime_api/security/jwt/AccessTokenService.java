package com.tempertime.tempertime_api.security.jwt;

import com.tempertime.tempertime_api.users.model.User;
import io.jsonwebtoken.Claims;

/** JWT access token creation and validation */
public interface AccessTokenService {

    String createAccessToken(User user);

    Claims validateAccessToken(String token);
}
