package com.tempertime.tempertime_api.common.error;

import com.tempertime.tempertime_api.auth.exception.EmailAlreadyExistsException;
import com.tempertime.tempertime_api.common.error.mapper.FieldErrorMapper;
import com.tempertime.tempertime_api.common.error.model.ApiError;
import com.tempertime.tempertime_api.common.error.model.ErrorCode;
import com.tempertime.tempertime_api.common.error.model.FieldError;
import com.tempertime.tempertime_api.security.exception.HashingException;
import com.tempertime.tempertime_api.security.exception.RefreshTokenExpiredException;
import com.tempertime.tempertime_api.security.exception.RefreshTokenNotFoundException;
import com.tempertime.tempertime_api.security.exception.RefreshTokenRevokedException;
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

/** Handles API exceptions and maps them to standard ApiError responses */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ApiErrorBuilder apiErrorBuilder;

    // ===================== Auth =====================

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.EMAIL_ALREADY_EXISTS,
                ex.getMessage(),
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
                "Invalid email or password",
                HttpStatus.UNAUTHORIZED,
                request
        );
    }

    // ===================== Security =====================

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleEmailNotFound(
            UsernameNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.EMAIL_NOT_FOUND,
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED,
                request
        );
    }

    @ExceptionHandler(HashingException.class)
    public ResponseEntity<ApiError> handleHashingFailed(
            HashingException ex,
            HttpServletRequest request
    ) {
        log.error("Hashing failed", ex);

        return buildErrorResponse(
                ErrorCode.INTERNAL_ERROR,
                "Internal security error",
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
                ex.getMessage(),
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
                ex.getMessage(),
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
                ex.getMessage(),
                HttpStatus.FORBIDDEN,
                request
        );
    }

    // ===================== Validation =====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.VALIDATION_ERROR,
                "Validation failed",
                HttpStatus.BAD_REQUEST,
                request,
                FieldErrorMapper.from(ex.getBindingResult())
        );
    }

    // ===================== Generic / Unexpected =====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception", ex);

        return buildErrorResponse(
                ErrorCode.INTERNAL_ERROR,
                "Unexpected error",
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
                .body(apiErrorBuilder.build(code, message, request.getRequestURI()));
    }

    private ResponseEntity<ApiError> buildErrorResponse(
            ErrorCode code,
            String message,
            HttpStatus status,
            HttpServletRequest request,
            List<FieldError> details
    ) {
        return ResponseEntity.status(status)
                .body(apiErrorBuilder.build(code, message, request.getRequestURI(), details));
    }
}
