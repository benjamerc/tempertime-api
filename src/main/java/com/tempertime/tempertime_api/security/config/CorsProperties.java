package com.tempertime.tempertime_api.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for CORS configuration.
 */
@Component
@ConfigurationProperties(prefix = "application.cors")
@Getter
@Setter
public class CorsProperties {

    private String allowedOrigins;
}
