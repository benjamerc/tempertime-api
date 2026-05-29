package com.tempertime.tempertime_api.workspaces.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Workspace representation returned after update operation.
 */
@Schema(
        description = "Updated workspace data"
)
public record WorkspaceUpdateResponse(

        @Schema(
                description = "Workspace unique identifier",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Workspace name",
                example = "Alpha Project"
        )
        String name,

        @Schema(
                description = "Workspace hex color code",
                example = "#2ECC71"
        )
        String color,

        @Schema(
                description = "Workspace creation timestamp in ISO 8601 (UTC)",
                example = "2026-02-07T10:15:30.339652Z"
        )
        Instant createdAt,

        @Schema(
                description = "Whether the workspace is archived",
                example = "false"
        )
        Boolean archived
) {}
