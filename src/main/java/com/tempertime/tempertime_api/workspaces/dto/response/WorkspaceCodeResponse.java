package com.tempertime.tempertime_api.workspaces.dto.response;

import java.time.Instant;

/** Workspace invitation code */
public record WorkspaceCodeResponse(

        Long id,
        String code,
        Boolean enabled,
        Instant createdAt
) {}
