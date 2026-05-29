package com.tempertime.tempertime_api.events.controller.docs;

import com.tempertime.tempertime_api.common.error.model.ApiError;
import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.events.domain.EventPeriod;
import com.tempertime.tempertime_api.events.dto.request.EventAssignUserRequest;
import com.tempertime.tempertime_api.events.dto.request.EventCreateRequest;
import com.tempertime.tempertime_api.events.dto.request.EventUpdateRequest;
import com.tempertime.tempertime_api.events.dto.response.*;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * OpenAPI documentation for event management operations.
 */
@Tag(
        name = "Events",
        description = "Event management and user assignment operations"
)
public interface EventControllerDocs {

    @Operation(
            summary = "Create event",
            description = "Creates a new event in a workspace"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Event created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or invalid event data",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation error",
                                            value = """
                                                {
                                                  "code": "VALIDATION_ERROR",
                                                  "message": "Validation failed for the request",
                                                  "path": "/api/workspaces/{workspaceId}/events",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": [
                                                    {
                                                      "field": "title",
                                                      "message": "Title must be between 3 and 150 characters"
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
                                                  "path": "/api/workspaces/{workspaceId}/events",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid event date format",
                                            value = """
                                                {
                                                  "code": "INVALID_EVENT_DATE_FORMAT",
                                                  "message": "The event date format is invalid. Use yyyy-MM-dd'T'HH:mm±HH:mm",
                                                  "path": "/api/workspaces/{workspaceId}/events",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid event date value",
                                            value = """
                                                {
                                                  "code": "INVALID_EVENT_DATE_VALUE",
                                                  "message": "The event date value is invalid. Please provide a real calendar date",
                                                  "path": "/api/workspaces/{workspaceId}/events",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Event date limit exceeded",
                                            value = """
                                                {
                                                  "code": "EVENT_DATE_LIMIT_EXCEEDED",
                                                  "message": "Event date exceeds the maximum allowed range",
                                                  "path": "/api/workspaces/{workspaceId}/events",
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
                                                  "path": "/api/workspaces/{workspaceId}/events",
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
                                                  "path": "/api/workspaces/{workspaceId}/events",
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
                                                  "path": "/api/workspaces/{workspaceId}/events",
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
                                                  "path": "/api/workspaces/{workspaceId}/events",
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
                                                  "path": "/api/workspaces/{workspaceId}/events",
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
                    description = "Conflict - workspace event limit exceeded",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace event limit exceeded",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_EVENT_LIMIT_EXCEEDED",
                                                  "message": "This workspace has reached its maximum event limit",
                                                  "path": "/api/workspaces/{workspaceId}/events",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<EventCreateResponse> createEvent(

            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId,

            @Valid @RequestBody EventCreateRequest request
    );

    @Operation(
            summary = "Get workspace events",
            description = "Returns all events belonging to a workspace"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Events retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid query parameter values",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid event period",
                                            value = """
                                                {
                                                  "code": "INVALID_EVENT_PERIOD",
                                                  "message": "Event period must be DAY, WEEK, MONTH or ALL",
                                                  "path": "/api/workspaces/{workspaceId}/events",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid time zone",
                                            value = """
                                                {
                                                  "code": "INVALID_TIME_ZONE",
                                                  "message": "Invalid time zone. Use a valid IANA time zone",
                                                  "path": "/api/workspaces/{workspaceId}/events",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Time zone missing",
                                            value = """
                                                {
                                                  "code": "TIME_ZONE_MISSING",
                                                  "message": "Time zone is required for DAY, WEEK, or MONTH periods",
                                                  "path": "/api/workspaces/{workspaceId}/events",
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
                                                  "path": "/api/workspaces/{workspaceId}/events",
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
                                                  "path": "/api/workspaces/{workspaceId}/events",
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
                                                  "path": "/api/workspaces/{workspaceId}/events",
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
                                                  "path": "/api/workspaces/{workspaceId}/events",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<PageResponse<EventListItemResponse>> getEvents(

            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId,

            @Parameter(
                    description = "Event period filter",
                    example = "MONTH"
            )
            @RequestParam(defaultValue = "MONTH") EventPeriod period,

            @Parameter(
                    description = "IANA time zone. Required when period is DAY, WEEK or MONTH",
                    example = "America/Argentina/Buenos_Aires"
            )
            @RequestParam(required = false) ZoneId timeZone,

            @Parameter(
                    description = "Base date used to calculate the selected period in ISO 8601 format (yyyy-MM-dd)",
                    example = "2026-03-15"
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @Parameter(hidden = true)
            Pageable pageable
    );

    @Operation(
            summary = "Get event",
            description = "Returns the details of a specific workspace event"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event retrieved successfully"
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Event access denied",
                                            value = """
                                                {
                                                  "code": "EVENT_ACCESS_DENIED",
                                                  "message": "You do not have access to this event",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
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
                    description = "Workspace or event not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Event not found",
                                            value = """
                                                {
                                                  "code": "EVENT_NOT_FOUND",
                                                  "message": "Event not found",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<EventResponse> getEvent(

            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId,

            @Parameter(description = "Event ID", example = "1")
            @PathVariable Long eventId
    );

    @Operation(
            summary = "Update event",
            description = "Partially updates an existing workspace event. Unsent fields remain unchanged"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or invalid event data",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation error",
                                            value = """
                                                {
                                                  "code": "VALIDATION_ERROR",
                                                  "message": "Validation failed for the request",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": [
                                                    {
                                                      "field": "title",
                                                      "message": "Title must be between 3 and 150 characters"
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid event date format",
                                            value = """
                                                {
                                                  "code": "INVALID_EVENT_DATE_FORMAT",
                                                  "message": "The event date format is invalid. Use yyyy-MM-dd'T'HH:mm±HH:mm",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid event date value",
                                            value = """
                                                {
                                                  "code": "INVALID_EVENT_DATE_VALUE",
                                                  "message": "The event date value is invalid. Please provide a real calendar date",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Event date limit exceeded",
                                            value = """
                                                {
                                                  "code": "EVENT_DATE_LIMIT_EXCEEDED",
                                                  "message": "Event date exceeds the maximum allowed range",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
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
                    description = "Workspace or event not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Event not found",
                                            value = """
                                                {
                                                  "code": "EVENT_NOT_FOUND",
                                                  "message": "Event not found",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<EventResponse> updateEvent(

            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId,

            @Parameter(description = "Event ID", example = "1")
            @PathVariable Long eventId,

            @Valid @RequestBody EventUpdateRequest request
    );

    @Operation(
            summary = "Delete event",
            description = "Permanently deletes a workspace event"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Event deleted successfully"
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
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
                    description = "Workspace or event not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Event not found",
                                            value = """
                                                {
                                                  "code": "EVENT_NOT_FOUND",
                                                  "message": "Event not found",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<Void> deleteEvent(

            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId,

            @Parameter(description = "Event ID", example = "1")
            @PathVariable Long eventId
    );

    @Operation(
            summary = "Assign users to event",
            description = "Assigns one or more workspace users to a specific event"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Users assigned successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation error",
                                            value = """
                                                {
                                                  "code": "VALIDATION_ERROR",
                                                  "message": "Validation failed for the request",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": [
                                                    {
                                                      "field": "userIds",
                                                      "message": "At least one user must be assigned to the event"
                                                    }
                                                  ]
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users",
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
                    description = "Forbidden - insufficient permissions or invalid workspace access",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace access denied",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_ACCESS_DENIED",
                                                  "message": "You do not have access to this workspace",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users",
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
                    description = "Workspace or event not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Event not found",
                                            value = """
                                                {
                                                  "code": "EVENT_NOT_FOUND",
                                                  "message": "Event not found",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users",
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
                    description = "Conflict - event cannot be assigned",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Event not assignable",
                                            value = """
                                                {
                                                  "code": "EVENT_NOT_ASSIGNABLE",
                                                  "message": "Users can only be assigned to SPECIFIC events",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<EventAssignUserResponse> assignUsersToEvent(

            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId,

            @Parameter(description = "Event ID", example = "1")
            @PathVariable Long eventId,

            @Valid @RequestBody EventAssignUserRequest request
    );

    @Operation(
            summary = "Get assigned event users",
            description = "Returns all users assigned to a specific event"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Assigned event users retrieved successfully"
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Event access denied",
                                            value = """
                                                {
                                                  "code": "EVENT_ACCESS_DENIED",
                                                  "message": "You do not have access to this event",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users",
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
                    description = "Workspace or event not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Event not found",
                                            value = """
                                                {
                                                  "code": "EVENT_NOT_FOUND",
                                                  "message": "Event not found",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<PageResponse<EventAssignedUserResponse>> getEventAssignedUsers(

            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId,

            @Parameter(description = "Event ID", example = "1")
            @PathVariable Long eventId,

            @Parameter(hidden = true)
            Pageable pageable
    );

    @Operation(
            summary = "Remove user from event",
            description = "Removes a user assignment from a specific event"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User removed from event successfully"
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users/{userId}",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users/{userId}",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users/{userId}",
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
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users/{userId}",
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
                    description = "Workspace, event or user assignment not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "User not assigned to event",
                                            value = """
                                                {
                                                  "code": "USER_NOT_ASSIGNED_TO_EVENT",
                                                  "message": "User is not assigned to this event",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users/{userId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Event not found",
                                            value = """
                                                {
                                                  "code": "EVENT_NOT_FOUND",
                                                  "message": "Event not found",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users/{userId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Workspace not found",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_NOT_FOUND",
                                                  "message": "Workspace not found",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users/{userId}",
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
                    description = "Conflict - event does not support user assignments",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Event not assignable",
                                            value = """
                                                {
                                                  "code": "EVENT_NOT_ASSIGNABLE",
                                                  "message": "Users can only be assigned to SPECIFIC events",
                                                  "path": "/api/workspaces/{workspaceId}/events/{eventId}/users/{userId}",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<Void> deleteUserFromEvent(

            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Long workspaceId,

            @Parameter(description = "Event ID", example = "1")
            @PathVariable Long eventId,

            @Parameter(description = "User ID", example = "2")
            @PathVariable("userId") Long targetUserId
    );
}
