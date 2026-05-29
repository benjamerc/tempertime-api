package com.tempertime.tempertime_api.auth.controller.docs;

import com.tempertime.tempertime_api.auth.dto.request.AuthLoginRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRefreshTokenRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRegisterRequest;
import com.tempertime.tempertime_api.auth.dto.response.AuthRegisterResponse;
import com.tempertime.tempertime_api.auth.dto.response.AuthTokenResponse;
import com.tempertime.tempertime_api.common.error.model.ApiError;
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
 * OpenAPI documentation for auth endpoints.
 */
@Tag(
        name = "Auth",
        description = "Authentication and token management operations"
)
public interface AuthControllerDocs {

    @Operation(
            summary = "Register a new user account",
            description = "Creates a new user account and registers access credentials for the system"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User account created successfully"
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
                                                  "path": "/api/auth/register",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": [
                                                    {
                                                      "field": "email",
                                                      "message": "Invalid email format"
                                                    },
                                                    {
                                                      "field": "firstName",
                                                      "message": "First name must be between 2 and 100 characters"
                                                    },
                                                    {
                                                      "field": "password",
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
                                                  "path": "/api/auth/register",
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
                    description = "Conflict - email already registered",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Email already exists",
                                            value = """
                                                {
                                                  "code": "EMAIL_ALREADY_EXISTS",
                                                  "message": "This email is already registered",
                                                  "path": "/api/auth/register",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<AuthRegisterResponse> register(
            @Valid @RequestBody AuthRegisterRequest request
    );

    @Operation(
            summary = "Authenticate user",
            description = "Authenticates the user and generates access credentials for the system"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful"
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
                                                  "path": "/api/auth/login",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": [
                                                    {
                                                      "field": "email",
                                                      "message": "Invalid email format"
                                                    },
                                                    {
                                                      "field": "password",
                                                      "message": "Password must be at most 255 characters"
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
                    description = "Unauthorized - invalid credentials",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Bad credentials",
                                            value = """
                                                {
                                                  "code": "BAD_CREDENTIALS",
                                                  "message": "Invalid email or password",
                                                  "path": "/api/auth/login",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<AuthTokenResponse> login(
            @Valid @RequestBody AuthLoginRequest request
    );

    @Operation(
            summary = "Refresh authentication tokens",
            description = "Generates new access and refresh tokens using a valid refresh token. The previous refresh token is invalidated due to token rotation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens refreshed successfully"
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
                                                  "path": "/api/auth/refresh",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": [
                                                    {
                                                      "field": "refreshToken",
                                                      "message": "Refresh token is required"
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
                    description = "Unauthorized - invalid or expired refresh token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Refresh token not found",
                                            value = """
                                                {
                                                  "code": "REFRESH_TOKEN_NOT_FOUND",
                                                  "message": "Refresh token not found",
                                                  "path": "/api/auth/refresh",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Refresh token expired",
                                            value = """
                                                {
                                                  "code": "REFRESH_TOKEN_EXPIRED",
                                                  "message": "The refresh token has expired",
                                                  "path": "/api/auth/refresh",
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
                    description = "Forbidden - refresh token has been revoked",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Refresh token revoked",
                                            value = """
                                                {
                                                  "code": "REFRESH_TOKEN_REVOKED",
                                                  "message": "Refresh token has been revoked",
                                                  "path": "/api/auth/refresh",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<AuthTokenResponse> refresh(
            @Valid @RequestBody AuthRefreshTokenRequest request
    );

    @Operation(
            summary = "Logout user",
            description = "Revokes the provided refresh token, invalidating the associated session"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout successful"
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
                                                  "path": "/api/auth/logout",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": [
                                                    {
                                                      "field": "refreshToken",
                                                      "message": "Refresh token is required"
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
                    description = "Unauthorized - invalid or expired refresh token",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Refresh token not found",
                                            value = """
                                                {
                                                  "code": "REFRESH_TOKEN_NOT_FOUND",
                                                  "message": "Refresh token not found",
                                                  "path": "/api/auth/logout",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Refresh token expired",
                                            value = """
                                                {
                                                  "code": "REFRESH_TOKEN_EXPIRED",
                                                  "message": "The refresh token has expired",
                                                  "path": "/api/auth/logout",
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
                    description = "Forbidden - refresh token has been revoked",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Refresh token revoked",
                                            value = """
                                                {
                                                  "code": "REFRESH_TOKEN_REVOKED",
                                                  "message": "Refresh token has been revoked",
                                                  "path": "/api/auth/logout",
                                                  "timestamp": "2026-02-08T08:15:30.339652Z",
                                                  "details": []
                                                }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<Void> logout(
            @Valid @RequestBody AuthRefreshTokenRequest request
    );
}
