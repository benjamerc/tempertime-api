package com.tempertime.tempertime_api.common.error.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** API error codes grouped by origin */
@AllArgsConstructor
@Getter
public enum ErrorCode {

    // Workspace
    WORKSPACE_ACCESS_DENIED("Access denied to workspace"),
    WORKSPACE_NOT_ARCHIVED("Workspace must be archived before deletion"),
    WORKSPACE_NOT_FOUND("Workspace not found"),
    WORKSPACE_ROLE_DENIED("User does not have sufficient permissions"),
    INVALID_WORKSPACE_INVITE_CODE("Invalid workspace invite code"),
    WORKSPACE_INVITE_CODE_NOT_FOUND("Workspace invite code not found"),
    WORKSPACE_INVITE_CODE_DISABLED("Workspace invite code is disabled"),
    USER_ALREADY_IN_WORKSPACE("User is already in workspace"),
    WORKSPACE_USER_NOT_FOUND("Workspace user not found"),
    WORKSPACE_OPERATION_NOT_ALLOWED("Workspace operation not allowed"),

    // Event
    INVALID_EVENT_DATE_FORMAT("Invalid event date format"),
    EVENT_ACCESS_DENIED("Access denied to event"),
    EVENT_NOT_ASSIGNABLE("Only SPECIFIC events can have users assigned"),
    USER_NOT_ASSIGNED_TO_EVENT("User is not assigned to the event"),
    EVENT_NOT_FOUND("Event not found"),
    INVALID_EVENT_PERIOD("Invalid event period"),

    // User
    INVALID_PASSWORD("Invalid password"),

    // Auth
    BAD_CREDENTIALS("Invalid email or password"),
    EMAIL_ALREADY_EXISTS("Email already exists"),

    // Security
    ACCESS_DENIED("Access denied"),
    ACCESS_TOKEN_INVALID("Invalid access token"),
    ACCESS_TOKEN_EXPIRED("Access token expired"),
    REFRESH_TOKEN_EXPIRED("Refresh token expired"),
    REFRESH_TOKEN_NOT_FOUND("Refresh token not found"),
    REFRESH_TOKEN_REVOKED("Refresh token revoked"),
    EMAIL_NOT_FOUND("Email not found"),

    // Common
    INVALID_COLOR_FORMAT("Invalid color format"),

    // Generic (Request layer)
    INVALID_TIME_ZONE("Invalid time zone. Use a valid IANA time zone"),
    INVALID_REQUEST_BODY("Malformed JSON request"),
    INVALID_REQUEST_PARAMETER("Invalid request parameter"),
    VALIDATION_ERROR("Validation error"),
    INTERNAL_ERROR("Internal server error");

    private final String defaultMessage;
}
