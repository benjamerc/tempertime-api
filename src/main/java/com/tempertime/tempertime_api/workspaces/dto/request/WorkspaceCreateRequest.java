package com.tempertime.tempertime_api.workspaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload used to create a new workspace.
 */
@Schema(
        description = "New workspace data"
)
public record WorkspaceCreateRequest(

        @Schema(
                description = "Workspace name",
                example = "Alpha Project"
        )
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Schema(
                description = "Workspace hex color code. If not specified, one is assigned automatically",
                example = "#3498DB",
                nullable = true
        )
        @Size(min = 4, max = 7, message = "Color must be between 4 and 7 characters")
        String color
) {}
