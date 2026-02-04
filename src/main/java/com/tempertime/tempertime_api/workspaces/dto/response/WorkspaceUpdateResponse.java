package com.tempertime.tempertime_api.workspaces.dto.response;

import java.time.Instant;

/** Workspace representation returned after update operation */
public record WorkspaceUpdateResponse(

        Long id,
        String name,
        String color,
        Instant createdAt,
        Boolean archived
) {}
