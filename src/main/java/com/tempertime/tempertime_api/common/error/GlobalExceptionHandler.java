package com.tempertime.tempertime_api.common.error;

import com.tempertime.tempertime_api.auth.exception.EmailAlreadyExistsException;
import com.tempertime.tempertime_api.common.color.InvalidColorFormatException;
import com.tempertime.tempertime_api.common.error.mapper.FieldErrorMapper;
import com.tempertime.tempertime_api.common.error.model.ApiError;
import com.tempertime.tempertime_api.common.error.model.ErrorCode;
import com.tempertime.tempertime_api.common.error.model.FieldError;
import com.tempertime.tempertime_api.events.exception.InvalidEventDateFormatException;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Maps exceptions to standardized ApiError responses.
 * Handlers are grouped by exception origin.
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

    @ExceptionHandler(InvalidEventDateFormatException.class)
    public ResponseEntity<ApiError> handleInvalidEventDateFormat(
            InvalidEventDateFormatException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.INVALID_EVENT_DATE_FORMAT,
                ErrorCode.INVALID_EVENT_DATE_FORMAT.getDefaultMessage(),
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
