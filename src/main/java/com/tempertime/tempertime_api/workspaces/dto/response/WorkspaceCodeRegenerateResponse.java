package com.tempertime.tempertime_api.workspaces.dto.response;

import java.time.Instant;

/** Workspace invitation code regeneration response */
public record WorkspaceCodeRegenerateResponse(
        String inviteCode,
        Boolean invitationsEnabled,
        Instant createdAt
) {}
