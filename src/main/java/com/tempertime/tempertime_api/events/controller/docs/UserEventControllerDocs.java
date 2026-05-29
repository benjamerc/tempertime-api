package com.tempertime.tempertime_api.events.controller.docs;

import com.tempertime.tempertime_api.common.error.model.ApiError;
import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.events.domain.EventPeriod;
import com.tempertime.tempertime_api.events.dto.response.UserEventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * OpenAPI documentation for user-event management operations.
 */
@Tag(
        name = "User Events",
        description = "Authenticated user's event assignment operations"
)
public interface UserEventControllerDocs {

    @Operation(
            summary = "Get authenticated user events",
            description = "Returns all events assigned to the authenticated user within the requested period. Optionally accepts a base date to calculate the period from."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Events retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid period or time zone",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid event period",
                                            value = """
                                                {
                                                  "code": "INVALID_EVENT_PERIOD",
                                                  "message": "Event period must be DAY, WEEK, MONTH or ALL",
                                                  "path": "/api/users/me/events",
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
                                                  "path": "/api/users/me/events",
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
                                                  "path": "/api/users/me/events",
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
                                                  "path": "/api/users/me/events",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<PageResponse<UserEventResponse>> getUserEvents(
            @Parameter(description = "Time period for filtering events. Case-insensitive.", example = "MONTH")
            @RequestParam(defaultValue = "MONTH") EventPeriod period,

            @Parameter(description = "IANA time zone. Required when period is DAY, WEEK or MONTH.", example = "America/Argentina/Buenos_Aires")
            @RequestParam(required = false) ZoneId timeZone,

            @Parameter(description = "Base date in ISO 8601 format (yyyy-MM-dd). If not provided, current date is used.", example = "2026-03-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @Parameter(hidden = true)
            Pageable pageable
    );
}