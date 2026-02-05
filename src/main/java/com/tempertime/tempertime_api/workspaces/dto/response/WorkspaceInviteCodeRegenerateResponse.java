package com.tempertime.tempertime_api.workspaces.dto.response;

import java.time.Instant;

/** Response returned after regenerating a workspace invite code */
public record WorkspaceInviteCodeRegenerateResponse(

        String inviteCode,
        Boolean inviteEnabled,
        Instant createdAt
) {}
