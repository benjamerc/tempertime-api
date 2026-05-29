package com.tempertime.tempertime_api.workspaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Payload used to update mutable workspace attributes.
 */
@Schema(
        description = "Mutable workspace fields to update"
)
public record WorkspaceUpdateRequest(

        @Schema(
                description = "Workspace name",
                example = "Alpha Project",
                nullable = true
        )
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Schema(
                description = "Workspace hex color code",
                example = "#2ECC71",
                nullable = true
        )
        @Size(min = 4, max = 7, message = "Color must be between 4 and 7 characters")
        String color
) {}
