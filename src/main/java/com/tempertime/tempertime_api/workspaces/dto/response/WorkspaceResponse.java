package com.tempertime.tempertime_api.workspaces.dto.response;

import java.time.Instant;

/** Workspace representation returned after create or update operations */
public record WorkspaceResponse(

        Long id,
        String name,
        String color,
        Instant createdAt,
        Boolean archived
) {}
