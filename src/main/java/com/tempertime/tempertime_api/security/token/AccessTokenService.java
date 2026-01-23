package com.tempertime.tempertime_api.security.token;

import com.tempertime.tempertime_api.security.config.JwtProperties;
import com.tempertime.tempertime_api.users.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service responsible for creating and validating JWT access tokens.
 * Uses JwtProperties for configuration and SecretKey for signing tokens.
 */
@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final JwtProperties jwtProperties;

    /**
     * Generates an access JWT for a user.
     * Includes: email (sub), id, role, issuedAt, and expiration.
     */
    public String createAccessToken(User user) {

        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("id", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates a JWT token and returns its claims.
     * Throws an exception if the token is invalid or expired.
     */
    public Claims validateAccessToken(String token) {

        Jws<Claims> jws = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);

        return jws.getPayload();
    }

    /**
     * Retrieves the secret key from configuration and converts it to a SecretKey for signing JWTs.
     * UTF-8 is used to ensure consistency across systems.
     */
    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }
}