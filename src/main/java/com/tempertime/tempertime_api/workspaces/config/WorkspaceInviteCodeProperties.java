package com.tempertime.tempertime_api.workspaces.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for invite code security settings.
 */
@Component
@ConfigurationProperties(prefix = "application.workspaces.invite-code")
@Getter
@Setter
public class WorkspaceInviteCodeProperties {

    private String secretKey;
}