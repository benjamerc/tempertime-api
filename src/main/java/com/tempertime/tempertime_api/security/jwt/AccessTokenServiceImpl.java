package com.tempertime.tempertime_api.security.jwt;

import com.tempertime.tempertime_api.security.config.JwtProperties;
import com.tempertime.tempertime_api.users.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {

    private final JwtProperties jwtProperties;

    /**
     * Generates a JWT access token for the given user.
     */
    @Override
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
     * Validates a JWT access token and returns its claims.
     */
    @Override
    public Claims validateAccessToken(String token) {

        Jws<Claims> jws = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);

        return jws.getPayload();
    }

    // Uses UTF-8 to derive a consistent HMAC signing key
    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }
}