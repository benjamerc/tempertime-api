package com.tempertime.tempertime_api.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for JWT.
 * Maps values from application.yml/.env:
 * - secretKey: secret used to sign tokens
 * - expiration: access token expiration in ms
 * - refreshToken.expiration: refresh token expiration in ms
 */
@Configuration
@ConfigurationProperties(prefix = "application.security.jwt")
@Data
public class JwtProperties {

    private String secretKey;
    private long expiration;
    private RefreshToken refreshToken;

    @Data
    public static class RefreshToken {
        private long expiration;
    }
}
