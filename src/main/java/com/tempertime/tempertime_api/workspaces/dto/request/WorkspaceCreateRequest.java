package com.tempertime.tempertime_api.workspaces.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload used to create a new workspace */
public record WorkspaceCreateRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Size(min = 4, max = 7, message = "Color must be between 4 and 7 characters")
        String color
) {}
