package com.tempertime.tempertime_api.workspaces.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for workspace settings.
 */
@Component
@ConfigurationProperties(prefix = "application.workspaces.constraints")
@Getter
@Setter
public class WorkspaceProperties {

    private int maxUsers;
}
