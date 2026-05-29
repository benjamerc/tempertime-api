package com.tempertime.tempertime_api.workspaces.dto.response;

import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Lightweight workspace view used in user workspace listings.
 */
@Schema(
        description = "Workspace summary including the authenticated user's role and archive status"
)
public record WorkspaceListItemResponse(

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
                example = "OWNER",
                allowableValues = {"OWNER", "MEMBER"}
        )
        WorkspaceRole userRole,

        @Schema(
                description = "Whether the workspace is archived",
                example = "false"
        )
        Boolean archived
) {}
