package com.tempertime.tempertime_api.workspaces.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Workspace representation returned after create operation.
 */
@Schema(
        description = "Created workspace data"
)
public record WorkspaceCreateResponse(

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
                description = "Unique alphanumeric code for inviting users to the workspace",
                example = "J9S53TVR3UKQ"
        )
        String inviteCode
) {}
