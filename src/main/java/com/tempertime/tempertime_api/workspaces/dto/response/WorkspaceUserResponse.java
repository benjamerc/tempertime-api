package com.tempertime.tempertime_api.workspaces.dto.response;

import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a workspace user with basic user information
 * and their role within the workspace.
 */
@Schema(
        description = "User information within a workspace"
)
public record WorkspaceUserResponse(

        @Schema(
                description = "User unique identifier",
                example = "1"
        )
        Long id,

        @Schema(
                description = "User first name",
                example = "John"
        )
        String firstName,

        @Schema(
                description = "User last name",
                example = "Doe"
        )
        String lastName,

        @Schema(
                description = "Role assigned to the user in the workspace",
                example = "OWNER"
        )
        WorkspaceRole role
) {}
