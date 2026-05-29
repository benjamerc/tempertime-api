package com.tempertime.tempertime_api.workspaces.dto.response;

import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Detailed workspace view for a user who is part of the workspace.
 */
@Schema(
        description = "Detailed workspace view including the authenticated user's role and archive status"
)
public record WorkspaceDetailResponse(

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
                example = "#E67E22"
        )
        String color,

        @Schema(
                description = "Authenticated user's role in this workspace",
                example = "OWNER", allowableValues = {"OWNER", "MEMBER"}
        )
        WorkspaceRole userRole,

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
