package com.tempertime.tempertime_api.common.error.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API error codes grouped primarily by origin.
 */
@AllArgsConstructor
@Getter
public enum ErrorCode {

    // Workspace
    WORKSPACE_ACCESS_DENIED("You do not have access to this workspace"),
    WORKSPACE_NOT_ARCHIVED("Workspace must be archived before deletion"),
    WORKSPACE_NOT_FOUND("Workspace not found"),
    WORKSPACE_ROLE_DENIED("Insufficient permissions for this workspace"),
    INVALID_WORKSPACE_INVITE_CODE("The workspace invite code is invalid"),
    WORKSPACE_INVITE_CODE_NOT_FOUND("Workspace invite code not found"),
    WORKSPACE_INVITE_CODE_DISABLED("This workspace invite code is disabled"),
    USER_ALREADY_IN_WORKSPACE("User already belongs to this workspace"),
    WORKSPACE_USER_NOT_FOUND("User not found in this workspace"),
    WORKSPACE_OPERATION_NOT_ALLOWED("This workspace operation is not allowed"),

    // Event
    INVALID_EVENT_DATE_FORMAT("The event date format is invalid. Use yyyy-MM-dd'T'HH:mm±HH:mm"),
    INVALID_EVENT_DATE_VALUE("The event date value is invalid. Please provide a real calendar date"),
    EVENT_ACCESS_DENIED("You do not have access to this event"),
    EVENT_NOT_ASSIGNABLE("Users can only be assigned to SPECIFIC events"),
    USER_NOT_ASSIGNED_TO_EVENT("User is not assigned to this event"),
    EVENT_NOT_FOUND("Event not found"),
    INVALID_EVENT_PERIOD("The event period is invalid"),

    // User
    INVALID_PASSWORD("The current password provided is incorrect"),
    USER_NOT_FOUND("User not found"),

    // Auth
    BAD_CREDENTIALS("Invalid email or password"),
    EMAIL_ALREADY_EXISTS("This email is already registered"),

    // Security
    ACCESS_DENIED("Access denied"),
    ACCESS_TOKEN_INVALID("The access token is invalid"),
    ACCESS_TOKEN_EXPIRED("The access token has expired"),
    REFRESH_TOKEN_EXPIRED("The refresh token has expired"),
    REFRESH_TOKEN_NOT_FOUND("Refresh token not found"),
    REFRESH_TOKEN_REVOKED("Refresh token has been revoked"),
    EMAIL_NOT_FOUND("Email address not found"),

    // Common
    INVALID_COLOR_FORMAT("The color format is invalid"),

    // Generic (Request layer)
    INVALID_TIME_ZONE("Invalid time zone. Use a valid IANA time zone"),
    INVALID_REQUEST_BODY("Malformed JSON in request body"),
    MISSING_REQUEST_PARAMETER("Missing required request parameter"),
    INVALID_REQUEST_PARAMETER("Invalid request parameter value"),
    VALIDATION_ERROR("Validation failed for the request"),
    INTERNAL_ERROR("Internal server error. Please try again later");

    private final String defaultMessage;
}
