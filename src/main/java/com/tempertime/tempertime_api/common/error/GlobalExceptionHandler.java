package com.tempertime.tempertime_api.common.error;

import com.tempertime.tempertime_api.auth.exception.EmailAlreadyExistsException;
import com.tempertime.tempertime_api.common.color.InvalidColorFormatException;
import com.tempertime.tempertime_api.common.error.mapper.FieldErrorMapper;
import com.tempertime.tempertime_api.common.error.model.ApiError;
import com.tempertime.tempertime_api.common.error.model.ErrorCode;
import com.tempertime.tempertime_api.common.error.model.FieldError;
import com.tempertime.tempertime_api.events.exception.*;
import com.tempertime.tempertime_api.events.query.exception.InvalidEventPeriodException;
import com.tempertime.tempertime_api.events.model.EventPeriod;
import com.tempertime.tempertime_api.security.exception.HashingException;
import com.tempertime.tempertime_api.security.exception.RefreshTokenExpiredException;
import com.tempertime.tempertime_api.security.exception.RefreshTokenNotFoundException;
import com.tempertime.tempertime_api.security.exception.RefreshTokenRevokedException;
import com.tempertime.tempertime_api.security.util.SecurityUtil;
import com.tempertime.tempertime_api.users.exception.InvalidPasswordException;
import com.tempertime.tempertime_api.workspaces.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.ZoneId;
import java.util.List;

/**
 * Maps exceptions to standardized ApiError responses.
 * Handlers are organized primarily by exception origin.
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ApiErrorBuilder apiErrorBuilder;

    // ===================== Workspace Exceptions =====================

    @ExceptionHandler(WorkspaceAccessDeniedException.class)
    public ResponseEntity<ApiError> handleWorkspaceAccessDenied(
            WorkspaceAccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.WORKSPACE_ACCESS_DENIED,
                ErrorCode.WORKSPACE_ACCESS_DENIED.getDefaultMessage(),
                HttpStatus.FORBIDDEN,
                request
        );
    }

    @ExceptionHandler(WorkspaceNotArchivedException.class)
    public ResponseEntity<ApiError> handleWorkspaceNotArchived(
            WorkspaceNotArchivedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.WORKSPACE_NOT_ARCHIVED,
                ErrorCode.WORKSPACE_NOT_ARCHIVED.getDefaultMessage(),
                HttpStatus.CONFLICT,
                request
        );
    }

    @ExceptionHandler(WorkspaceNotFoundException.class)
    public ResponseEntity<ApiError> handleWorkspaceNotFound(
            WorkspaceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.WORKSPACE_NOT_FOUND,
                ErrorCode.WORKSPACE_NOT_FOUND.getDefaultMessage(),
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @ExceptionHandler(WorkspaceUserNotFoundException.class)
    public ResponseEntity<ApiError> handleWorkspaceMemberNotFound(
            WorkspaceUserNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.WORKSPACE_USER_NOT_FOUND,
                ErrorCode.WORKSPACE_USER_NOT_FOUND.getDefaultMessage(),
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @ExceptionHandler(WorkspaceRoleDeniedException.class)
    public ResponseEntity<ApiError> handleWorkspaceRoleDenied(
            WorkspaceRoleDeniedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.WORKSPACE_ROLE_DENIED,
                ErrorCode.WORKSPACE_ROLE_DENIED.getDefaultMessage(),
                HttpStatus.FORBIDDEN,
                request
        );
    }

    @ExceptionHandler(WorkspaceOperationNotAllowedException.class)
    public ResponseEntity<ApiError> handleWorkspaceOperationNotAllowed(
            WorkspaceOperationNotAllowedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.WORKSPACE_OPERATION_NOT_ALLOWED,
                ErrorCode.WORKSPACE_OPERATION_NOT_ALLOWED.getDefaultMessage(),
                HttpStatus.FORBIDDEN,
                request
        );
    }

    // ===================== Workspace Invite Code Exceptions =====================

    @ExceptionHandler(WorkspaceInviteCodeGenerationException.class)
    public ResponseEntity<ApiError> handleWorkspaceInviteCodeGeneration(
            WorkspaceInviteCodeGenerationException ex,
            HttpServletRequest request
    ) {
        log.error("Workspace invite code generation failed", ex);

        return buildErrorResponse(
                ErrorCode.INTERNAL_ERROR,
                ErrorCode.INTERNAL_ERROR.getDefaultMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    @ExceptionHandler(InvalidWorkspaceInviteCodeException.class)
    public ResponseEntity<ApiError> handleInvalidWorkspaceInviteCode(
            InvalidWorkspaceInviteCodeException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.INVALID_WORKSPACE_INVITE_CODE,
                ErrorCode.INVALID_WORKSPACE_INVITE_CODE.getDefaultMessage(),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(WorkspaceInviteCodeNotFoundException.class)
    public ResponseEntity<ApiError> handleWorkspaceInviteCodeNotFound(
            WorkspaceInviteCodeNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.WORKSPACE_INVITE_CODE_NOT_FOUND,
                ErrorCode.WORKSPACE_INVITE_CODE_NOT_FOUND.getDefaultMessage(),
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @ExceptionHandler(WorkspaceInviteCodeDisabledException.class)
    public ResponseEntity<ApiError> handleWorkspaceInviteCodeDisabled(
            WorkspaceInviteCodeDisabledException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.WORKSPACE_INVITE_CODE_DISABLED,
                ErrorCode.WORKSPACE_INVITE_CODE_DISABLED.getDefaultMessage(),
                HttpStatus.CONFLICT,
                request
        );
    }

    @ExceptionHandler(UserAlreadyInWorkspaceException.class)
    public ResponseEntity<ApiError> handleUserAlreadyInWorkspace(
            UserAlreadyInWorkspaceException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.USER_ALREADY_IN_WORKSPACE,
                ErrorCode.USER_ALREADY_IN_WORKSPACE.getDefaultMessage(),
                HttpStatus.CONFLICT,
                request
        );
    }

    // ===================== Event Exceptions =====================

    @ExceptionHandler(EventAccessDeniedException.class)
    public ResponseEntity<ApiError> handleEventAccessDenied(
            EventAccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.EVENT_ACCESS_DENIED,
                ErrorCode.EVENT_ACCESS_DENIED.getDefaultMessage(),
                HttpStatus.FORBIDDEN,
                request
        );
    }

    @ExceptionHandler(EventNotAssignableException.class)
    public ResponseEntity<ApiError> handleEventNotAssignable(
            EventNotAssignableException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.EVENT_NOT_ASSIGNABLE,
                ErrorCode.EVENT_NOT_ASSIGNABLE.getDefaultMessage(),
                HttpStatus.CONFLICT,
                request
        );
    }

    @ExceptionHandler(UserNotAssignedToEventException.class)
    public ResponseEntity<ApiError> handleUserNotAssignedToEvent(
            UserNotAssignedToEventException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.USER_NOT_ASSIGNED_TO_EVENT,
                ErrorCode.USER_NOT_ASSIGNED_TO_EVENT.getDefaultMessage(),
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ApiError> handleEventNotFound(
            EventNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.EVENT_NOT_FOUND,
                ErrorCode.EVENT_NOT_FOUND.getDefaultMessage(),
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @ExceptionHandler(InvalidEventPeriodException.class)
    public ResponseEntity<ApiError> handleInvalidEventPeriod(
            InvalidEventPeriodException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.INVALID_EVENT_PERIOD,
                ErrorCode.INVALID_EVENT_PERIOD.getDefaultMessage(),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    // ===================== User Exceptions =====================

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiError> handleInvalidPassword(
            InvalidPasswordException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.INVALID_PASSWORD,
                ErrorCode.INVALID_PASSWORD.getDefaultMessage(),
                HttpStatus.FORBIDDEN,
                request
        );
    }

    // ===================== Auth Exceptions =====================

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.EMAIL_ALREADY_EXISTS,
                ErrorCode.EMAIL_ALREADY_EXISTS.getDefaultMessage(),
                HttpStatus.CONFLICT,
                request
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.BAD_CREDENTIALS,
                ErrorCode.BAD_CREDENTIALS.getDefaultMessage(),
                HttpStatus.UNAUTHORIZED,
                request
        );
    }

    // ===================== Security Exceptions =====================

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleEmailNotFound(
            UsernameNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.EMAIL_NOT_FOUND,
                ErrorCode.EMAIL_NOT_FOUND.getDefaultMessage(),
                HttpStatus.UNAUTHORIZED,
                request
        );
    }

    // ===================== Security / Refresh Tokens Exceptions =====================

    @ExceptionHandler(HashingException.class)
    public ResponseEntity<ApiError> handleHashingFailed(
            HashingException ex,
            HttpServletRequest request
    ) {
        log.error("Hashing failed", ex);

        return buildErrorResponse(
                ErrorCode.INTERNAL_ERROR,
                ErrorCode.INTERNAL_ERROR.getDefaultMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ApiError> handleRefreshTokenExpired(
            RefreshTokenExpiredException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.REFRESH_TOKEN_EXPIRED,
                ErrorCode.REFRESH_TOKEN_EXPIRED.getDefaultMessage(),
                HttpStatus.UNAUTHORIZED,
                request
        );
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ApiError> handleRefreshTokenNotFound(
            RefreshTokenNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.REFRESH_TOKEN_NOT_FOUND,
                ErrorCode.REFRESH_TOKEN_NOT_FOUND.getDefaultMessage(),
                HttpStatus.UNAUTHORIZED,
                request
        );
    }

    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<ApiError> handleRefreshTokenRevoked(
            RefreshTokenRevokedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.REFRESH_TOKEN_REVOKED,
                ErrorCode.REFRESH_TOKEN_REVOKED.getDefaultMessage(),
                HttpStatus.FORBIDDEN,
                request
        );
    }

    // ===================== Common Exceptions =====================

    @ExceptionHandler(InvalidColorFormatException.class)
    public ResponseEntity<ApiError> handleInvalidColorFormat(
            InvalidColorFormatException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.INVALID_COLOR_FORMAT,
                ErrorCode.INVALID_COLOR_FORMAT.getDefaultMessage(),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    // ===================== Request Deserialization Exceptions =====================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        Throwable cause = ex.getCause();

        while (cause != null && !(cause instanceof InvalidEventDateFormatException)) {
            cause = cause.getCause();
        }

        if (cause instanceof InvalidEventDateFormatException invalidEx) {
            return buildErrorResponse(
                    ErrorCode.INVALID_EVENT_DATE_FORMAT,
                    invalidEx.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    request
            );
        }

        return buildErrorResponse(
                ErrorCode.INVALID_REQUEST_BODY,
                ErrorCode.INVALID_REQUEST_BODY.getDefaultMessage(),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    // ===================== Request Parameter Exceptions =====================

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.MISSING_REQUEST_PARAMETER,
                ErrorCode.MISSING_REQUEST_PARAMETER.getDefaultMessage(),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {

        if (ex.getRequiredType() == ZoneId.class) {

            return buildErrorResponse(
                    ErrorCode.INVALID_TIME_ZONE,
                    ErrorCode.INVALID_TIME_ZONE.getDefaultMessage(),
                    HttpStatus.BAD_REQUEST,
                    request
            );
        }

        if (ex.getRequiredType() == EventPeriod.class) {

            return buildErrorResponse(
                    ErrorCode.INVALID_EVENT_PERIOD,
                    ErrorCode.INVALID_EVENT_PERIOD.getDefaultMessage(),
                    HttpStatus.BAD_REQUEST,
                    request
            );
        }

        return buildErrorResponse(
                ErrorCode.INVALID_REQUEST_PARAMETER,
                ErrorCode.INVALID_REQUEST_PARAMETER.getDefaultMessage(),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    // ===================== Validation Exceptions =====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
                HttpStatus.BAD_REQUEST,
                request,
                FieldErrorMapper.from(ex.getBindingResult())
        );
    }

    // ===================== Generic Exceptions =====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception", ex);

        return buildErrorResponse(
                ErrorCode.INTERNAL_ERROR,
                ErrorCode.INTERNAL_ERROR.getDefaultMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    private ResponseEntity<ApiError> buildErrorResponse(
            ErrorCode code,
            String message,
            HttpStatus status,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status)
                .body(apiErrorBuilder.build(code, message, SecurityUtil.resolveRequestPath(request)));
    }

    private ResponseEntity<ApiError> buildErrorResponse(
            ErrorCode code,
            String message,
            HttpStatus status,
            HttpServletRequest request,
            List<FieldError> details
    ) {
        return ResponseEntity.status(status)
                .body(apiErrorBuilder.build(code, message, SecurityUtil.resolveRequestPath(request), details));
    }
}
