package com.tempertime.tempertime_api.workspaces.controller.docs;

import com.tempertime.tempertime_api.common.error.model.ApiError;
import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceCreateRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceJoinRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceUpdateRequest;
import com.tempertime.tempertime_api.workspaces.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * OpenAPI documentation for workspace management endpoints.
 */
@Tag(
        name = "Workspaces",
        description = "Workspace management and collaboration operations"
)
public interface WorkspaceControllerDocs {

    @Operation(
            summary = "Create workspace",
            description = "Creates a new workspace in the system. If no color is specified, one is assigned automatically"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Workspace created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or invalid color format",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation error",
                                            value = """
                                                {
                                                  "code": "VALIDATION_ERROR",
                                                  "message": "Validation failed for the request",
                                                  "path": "/api/workspaces",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": [
                                                    {
                                                      "field": "name",
                                                      "message": "Name must be between 2 and 100 characters"
                                                    }
                                                  ]
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid color format",
                                            value = """
                                                {
                                                  "code": "INVALID_COLOR_FORMAT",
                                                  "message": "Invalid color format. Use a valid hexadecimal color code (#RRGGBB or #RGB)",
                                                  "path": "/api/workspaces",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access token invalid",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_INVALID",
                                                  "message": "The access token is invalid",
                                                  "path": "/api/workspaces",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Access token expired",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_EXPIRED",
                                                  "message": "The access token has expired",
                                                  "path": "/api/workspaces",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<WorkspaceCreateResponse> createWorkspace(@Valid @RequestBody WorkspaceCreateRequest request);

    @Operation(
            summary = "Get user workspaces",
            description = "Returns all workspaces the authenticated user belongs to"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workspaces retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access token invalid",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_INVALID",
                                                  "message": "The access token is invalid",
                                                  "path": "/api/workspaces",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Access token expired",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_EXPIRED",
                                                  "message": "The access token has expired",
                                                  "path": "/api/workspaces",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<PageResponse<WorkspaceListItemResponse>> getUserWorkspaces(
            @Parameter(description = "Filter by workspace role. If not specified, all roles are included.", example = "OWNER")
            @RequestParam(required = false) WorkspaceRole role,

            @Parameter(description = "Filter by archived status. If not specified, all statuses are included.", example = "false")
            @RequestParam(required = false) Boolean archived,

            @Parameter(hidden = true)
            Pageable pageable
    );

    @Operation(
            summary = "Get workspace by ID",
            description = "Returns the details of a specific workspace the authenticated user belongs to"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workspace retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access token invalid",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_INVALID",
                                                  "message": "The access token is invalid",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Access token expired",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_EXPIRED",
                                                  "message": "The access token has expired",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user does not belong to this workspace",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace access denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ACCESS_DENIED",
                                                  "message": "You do not have access to this workspace",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workspace not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<WorkspaceDetailResponse> getWorkspaceById(
            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId
    );

    @Operation(
            summary = "Update workspace",
            description = "Partially updates a workspace. Unsent fields remain unchanged"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workspace updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or invalid color format",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation error",
                                            value = """
                                                {
                                                  "code": "VALIDATION_ERROR",
                                                  "message": "Validation failed for the request",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": [
                                                    {
                                                      "field": "name",
                                                      "message": "Name must be between 2 and 100 characters"
                                                    }
                                                  ]
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid color format",
                                            value = """
                                                {
                                                  "code": "INVALID_COLOR_FORMAT",
                                                  "message": "Invalid color format. Use a valid hexadecimal color code (#RRGGBB or #RGB)",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access token invalid",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_INVALID",
                                                  "message": "The access token is invalid",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Access token expired",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_EXPIRED",
                                                  "message": "The access token has expired",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace access denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ACCESS_DENIED",
                                                  "message": "You do not have access to this workspace",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace role denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ROLE_DENIED",
                                                  "message": "Insufficient permissions for this workspace",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workspace not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<WorkspaceUpdateResponse> updateWorkspace(
            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId,
            @Valid @RequestBody WorkspaceUpdateRequest request
    );

    @Operation(
            summary = "Archive workspace",
            description = "Archives a workspace. Data is preserved and the workspace can be unarchived later"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Workspace archived successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access token invalid",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_INVALID",
                                                  "message": "The access token is invalid",
                                                  "path": "/api/workspaces/{workspaceId}/archive",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Access token expired",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_EXPIRED",
                                                  "message": "The access token has expired",
                                                  "path": "/api/workspaces/{workspaceId}/archive",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace access denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ACCESS_DENIED",
                                                  "message": "You do not have access to this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/archive",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace role denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ROLE_DENIED",
                                                  "message": "Insufficient permissions for this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/archive",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workspace not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}/archive",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<Void> archiveWorkspace(
            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId
    );

    @Operation(
            summary = "Unarchive workspace",
            description = "Restores an archived workspace to active status"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Workspace unarchived successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access token invalid",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_INVALID",
                                                  "message": "The access token is invalid",
                                                  "path": "/api/workspaces/{workspaceId}/unarchive",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Access token expired",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_EXPIRED",
                                                  "message": "The access token has expired",
                                                  "path": "/api/workspaces/{workspaceId}/unarchive",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace access denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ACCESS_DENIED",
                                                  "message": "You do not have access to this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/unarchive",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace role denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ROLE_DENIED",
                                                  "message": "Insufficient permissions for this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/unarchive",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workspace not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}/unarchive",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<Void> unarchiveWorkspace(
            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId
    );

    @Operation(
            summary = "Delete workspace",
            description = "Permanently deletes a workspace. The workspace must be archived before deletion"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Workspace deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access token invalid",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_INVALID",
                                                  "message": "The access token is invalid",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Access token expired",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_EXPIRED",
                                                  "message": "The access token has expired",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace access denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ACCESS_DENIED",
                                                  "message": "You do not have access to this workspace",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace role denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ROLE_DENIED",
                                                  "message": "Insufficient permissions for this workspace",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workspace not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - workspace must be archived before deletion",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not archived",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_ARCHIVED",
                                                  "message": "Workspace must be archived before deletion",
                                                  "path": "/api/workspaces/{workspaceId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<Void> deleteWorkspace(
            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId
    );

    @Operation(
            summary = "Get workspace invite code",
            description = "Returns the invite code associated with a workspace"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workspace invite code retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access token invalid",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_INVALID",
                                                  "message": "The access token is invalid",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Access token expired",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_EXPIRED",
                                                  "message": "The access token has expired",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace access denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ACCESS_DENIED",
                                                  "message": "You do not have access to this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace role denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ROLE_DENIED",
                                                  "message": "Insufficient permissions for this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workspace or invite code not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace invite code not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_INVITE_CODE_NOT_FOUND",
                                                  "message": "Workspace invite code not found",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<WorkspaceInviteCodeResponse> getInviteCode(
            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId
    );

    @Operation(
            summary = "Enable workspace invite code",
            description = "Enables the workspace invite code, allowing new users to join the workspace"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workspace invite code enabled successfully",
                    content = @Content(
                            schema = @Schema(implementation = WorkspaceInviteCodeStatusResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "inviteEnabled": true
                                        }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access token invalid",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_INVALID",
                                                  "message": "The access token is invalid",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/enable",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Access token expired",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_EXPIRED",
                                                  "message": "The access token has expired",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/enable",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace access denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ACCESS_DENIED",
                                                  "message": "You do not have access to this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/enable",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace role denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ROLE_DENIED",
                                                  "message": "Insufficient permissions for this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/enable",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workspace or invite code not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/enable",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace invite code not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_INVITE_CODE_NOT_FOUND",
                                                  "message": "Workspace invite code not found",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/enable",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<WorkspaceInviteCodeStatusResponse> activateInviteCode(
            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId
    );

    @Operation(
            summary = "Disable workspace invite code",
            description = "Disables the workspace invite code, preventing new users from joining the workspace"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workspace invite code disabled successfully",
                    content = @Content(
                            schema = @Schema(implementation = WorkspaceInviteCodeStatusResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "inviteEnabled": false
                                        }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access token invalid",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_INVALID",
                                                  "message": "The access token is invalid",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/disable",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Access token expired",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_EXPIRED",
                                                  "message": "The access token has expired",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/disable",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace access denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ACCESS_DENIED",
                                                  "message": "You do not have access to this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/disable",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace role denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ROLE_DENIED",
                                                  "message": "Insufficient permissions for this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/disable",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workspace or invite code not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/disable",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace invite code not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_INVITE_CODE_NOT_FOUND",
                                                  "message": "Workspace invite code not found",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/disable",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<WorkspaceInviteCodeStatusResponse> deactivateInviteCode(
            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId
    );

    @Operation(
            summary = "Regenerate workspace invite code",
            description = "Generates a new random invite code for the workspace"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workspace invite code regenerated successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access token invalid",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_INVALID",
                                                  "message": "The access token is invalid",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/regenerate",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Access token expired",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_EXPIRED",
                                                  "message": "The access token has expired",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/regenerate",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace access denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ACCESS_DENIED",
                                                  "message": "You do not have access to this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/regenerate",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace role denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ROLE_DENIED",
                                                  "message": "Insufficient permissions for this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/regenerate",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workspace or invite code not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/regenerate",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace invite code not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_INVITE_CODE_NOT_FOUND",
                                                  "message": "Workspace invite code not found",
                                                  "path": "/api/workspaces/{workspaceId}/invite-code/regenerate",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<WorkspaceInviteCodeResponse> regenerateInviteCode(
            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId
    );

    @Operation(
            summary = "Join workspace",
            description = "Allows an authenticated user to join a workspace using an invite code"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User joined workspace successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or invalid invite code",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation error",
                                            value = """
                                                {
                                                  "code": "VALIDATION_ERROR",
                                                  "message": "Validation failed for the request",
                                                  "path": "/api/workspaces/join",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": [
                                                    {
                                                      "field": "inviteCode",
                                                      "message": "Invite code must be 12 characters"
                                                    }
                                                  ]
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid workspace invite code",
                                            value = """
                                                {
                                                  "code": "INVALID_WORKSPACE_INVITE_CODE",
                                                  "message": "The workspace invite code is invalid",
                                                  "path": "/api/workspaces/join",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access token invalid",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_INVALID",
                                                  "message": "The access token is invalid",
                                                  "path": "/api/workspaces/join",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Access token expired",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_EXPIRED",
                                                  "message": "The access token has expired",
                                                  "path": "/api/workspaces/join",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - workspace join operation failed",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace invite code disabled",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_INVITE_CODE_DISABLED",
                                                  "message": "This workspace invite code is disabled",
                                                  "path": "/api/workspaces/join",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "User already in workspace",
                                            value = """
                                                {
                                                  "code": "USER_ALREADY_IN_WORKSPACE",
                                                  "message": "User already belongs to this workspace",
                                                  "path": "/api/workspaces/join",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace capacity exceeded",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_CAPACITY_EXCEEDED",
                                                  "message": "This workspace has reached its maximum user capacity",
                                                  "path": "/api/workspaces/join",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<WorkspaceJoinResponse> joinWorkspace(
            @Valid @RequestBody WorkspaceJoinRequest request
    );

    @Operation(
            summary = "Get workspace users",
            description = "Returns all users belonging to a workspace"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workspace users retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access token invalid",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_INVALID",
                                                  "message": "The access token is invalid",
                                                  "path": "/api/workspaces/{workspaceId}/users",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Access token expired",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_EXPIRED",
                                                  "message": "The access token has expired",
                                                  "path": "/api/workspaces/{workspaceId}/users",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user does not belong to this workspace",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace access denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ACCESS_DENIED",
                                                  "message": "You do not have access to this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/users",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workspace not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}/users",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<PageResponse<WorkspaceUserResponse>> getWorkspaceUsers(

            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId,

            @Parameter(hidden = true)
            Pageable pageable
    );

    @Operation(
            summary = "Remove workspace user",
            description = "Removes a user from a workspace"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Workspace user removed successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access token invalid",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_INVALID",
                                                  "message": "The access token is invalid",
                                                  "path": "/api/workspaces/{workspaceId}/users/{userId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Access token expired",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_EXPIRED",
                                                  "message": "The access token has expired",
                                                  "path": "/api/workspaces/{workspaceId}/users/{userId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions or operation not allowed",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace access denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ACCESS_DENIED",
                                                  "message": "You do not have access to this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/users/{userId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace role denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ROLE_DENIED",
                                                  "message": "Insufficient permissions for this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/users/{userId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace operation not allowed",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_OPERATION_NOT_ALLOWED",
                                                  "message": "This workspace operation is not allowed",
                                                  "path": "/api/workspaces/{workspaceId}/users/{userId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workspace or workspace user not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}/users/{userId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace user not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_USER_NOT_FOUND",
                                                  "message": "User not found in this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/users/{userId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<Void> removeWorkspaceUser(

            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId,

            @Parameter(description = "User ID to remove from the workspace", example = "2")
            @PathVariable("userId") Long targetUserId
    );

    @Operation(
            summary = "Leave workspace",
            description = "Allows an authenticated user to leave a workspace"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User left workspace successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access token invalid",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_INVALID",
                                                  "message": "The access token is invalid",
                                                  "path": "/api/workspaces/{workspaceId}/users/me",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Access token expired",
                                            value = """
                                                {
                                                  "code": "ACCESS_TOKEN_EXPIRED",
                                                  "message": "The access token has expired",
                                                  "path": "/api/workspaces/{workspaceId}/users/me",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - operation not allowed",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace access denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ACCESS_DENIED",
                                                  "message": "You do not have access to this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/users/me",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace operation not allowed",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_OPERATION_NOT_ALLOWED",
                                                  "message": "This workspace operation is not allowed",
                                                  "path": "/api/workspaces/{workspaceId}/users/me",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workspace not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}/users/me",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<Void> leaveWorkspace(
            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId
    );
}
