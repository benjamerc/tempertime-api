package com.tempertime.tempertime_api.workspaces.dto.response;

import java.time.Instant;

/**
 * Represents a workspace invite code.
 */
public record WorkspaceInviteCodeResponse(

        Boolean inviteEnabled,
        Instant createdAt
) {}
