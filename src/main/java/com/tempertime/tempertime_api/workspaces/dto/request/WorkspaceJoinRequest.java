package com.tempertime.tempertime_api.workspaces.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to join a workspace using an invite code.
 */
public record WorkspaceJoinRequest(

        @NotBlank(message = "Invite code is required")
        @Size(min = 12, max = 12, message = "Invite code must be 12 characters")
        String inviteCode
) {}
