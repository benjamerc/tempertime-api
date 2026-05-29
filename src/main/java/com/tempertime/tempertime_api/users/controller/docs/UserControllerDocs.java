package com.tempertime.tempertime_api.users.controller.docs;

import com.tempertime.tempertime_api.common.error.model.ApiError;
import com.tempertime.tempertime_api.users.dto.request.UserDeleteAccountRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdatePasswordRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdateProfileRequest;
import com.tempertime.tempertime_api.users.dto.response.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * OpenAPI documentation for user management endpoints.
 */
@Tag(
        name = "Users",
        description = "User account and profile operations"
)
public interface UserControllerDocs {

    @Operation(
            summary = "Get current user profile",
            description = "Returns the authenticated user's profile information"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully"
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
                                                  "path": "/api/users/me",
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
                                                  "path": "/api/users/me",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<UserProfileResponse> userProfile();

    @Operation(
            summary = "Update current user profile",
            description = "Partially updates the authenticated user's profile. Unsent fields remain unchanged"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = UserProfileResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error - invalid request data",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation error",
                                            value = """
                                                {
                                                  "code": "VALIDATION_ERROR",
                                                  "message": "Validation failed for the request",
                                                  "path": "/api/users/me",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": [
                                                    {
                                                      "field": "firstName",
                                                      "message": "First name must be between 2 and 100 characters"
                                                    },
                                                    {
                                                      "field": "lastName",
                                                      "message": "Last name must be between 2 and 100 characters"
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
                                                  "path": "/api/users/me",
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
                                                  "path": "/api/users/me",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UserUpdateProfileRequest request);

    @Operation(
            summary = "Update current user password",
            description = "Updates the authenticated user's password"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Password updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error - invalid request data or password format",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation error",
                                            value = """
                                                {
                                                  "code": "VALIDATION_ERROR",
                                                  "message": "Validation failed for the request",
                                                  "path": "/api/users/me/password",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": [
                                                    {
                                                      "field": "newPassword",
                                                      "message": "Password must be between 8 and 255 characters"
                                                    }
                                                  ]
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid password format",
                                            value = """
                                                {
                                                  "code": "INVALID_PASSWORD_FORMAT",
                                                  "message": "The password must be at least 8 characters long, include at least one uppercase letter, one lowercase letter, one number, and must not contain whitespace characters",
                                                  "path": "/api/users/me/password",
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
                                                  "path": "/api/users/me/password",
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
                                                  "path": "/api/users/me/password",
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
                    description = "Forbidden - current password is incorrect",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid current password",
                                            value = """
                                                {
                                                  "code": "INVALID_PASSWORD",
                                                  "message": "The current password provided is incorrect",
                                                  "path": "/api/users/me/password",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<Void> updatePassword(@Valid @RequestBody UserUpdatePasswordRequest request);

    @Operation(
            summary = "Delete current user account",
            description = "Permanently deletes the authenticated user's account"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Account deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error - invalid request data",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation error",
                                            value = """
                                                {
                                                  "code": "VALIDATION_ERROR",
                                                  "message": "Validation failed for the request",
                                                  "path": "/api/users/me",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": [
                                                    {
                                                      "field": "currentPassword",
                                                      "message": "Current password is required"
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
                                                  "path": "/api/users/me",
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
                                                  "path": "/api/users/me",
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
                    description = "Forbidden - current password is incorrect",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid current password",
                                            value = """
                                                {
                                                  "code": "INVALID_PASSWORD",
                                                  "message": "The current password provided is incorrect",
                                                  "path": "/api/users/me",
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
                    description = "Conflict - account deletion restricted",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Workspace owner restriction",
                                            value = """
                                                {
                                                  "code": "WORKSPACE_OWNER_RESTRICTION",
                                                  "message": "Account deletion is restricted while owning one or more workspaces",
                                                  "path": "/api/users/me",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<Void> deleteAccount(@Valid @RequestBody UserDeleteAccountRequest request);
}
