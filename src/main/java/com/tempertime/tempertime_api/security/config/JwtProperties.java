package com.tempertime.tempertime_api.security.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** JWT security configuration properties */
@Configuration
@ConfigurationProperties(prefix = "application.security.jwt")
@Data
public class JwtProperties {

    private String secretKey;
    private long expiration;
    private RefreshToken refreshToken;

    @PostConstruct
    void validate() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("JWT secret key is not configured");
        }
        if (expiration <= 0) {
            throw new IllegalStateException("JWT expiration must be > 0");
        }
        if (refreshToken == null || refreshToken.getExpiration() <= 0) {
            throw new IllegalStateException("JWT refresh token expiration must be > 0");
        }
    }

    @Data
    public static class RefreshToken {
        private long expiration;
    }
}
