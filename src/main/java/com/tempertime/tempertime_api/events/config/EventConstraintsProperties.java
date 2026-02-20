package com.tempertime.tempertime_api.events.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for event date constraints.
 *
 * Binds and exposes settings defined under the
 * "application.events.constraints" prefix.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "application.events.constraints")
public class EventConstraintsProperties {

    private int maxMonthsAhead;
}
