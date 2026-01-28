package com.tempertime.tempertime_api.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tempertime.tempertime_api.common.error.ApiErrorBuilder;
import com.tempertime.tempertime_api.common.error.model.ApiError;
import com.tempertime.tempertime_api.common.error.model.ErrorCode;
import com.tempertime.tempertime_api.security.exception.AccessTokenExpiredException;
import com.tempertime.tempertime_api.security.util.SecurityAttributes;
import com.tempertime.tempertime_api.security.util.SecurityUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles unauthenticated requests, returning a standardized 401 JSON response.
 * Determines if the failure is due to an expired or invalid access token.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final ApiErrorBuilder apiErrorBuilder;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException ex
    ) throws IOException {

        Object authError = request.getAttribute(SecurityAttributes.SECURITY_ERROR_CODE.name());

        ErrorCode errorCode =
                authError instanceof ErrorCode
                        ? (ErrorCode) authError
                        : ErrorCode.ACCESS_TOKEN_INVALID;

        String path = SecurityUtil.resolveRequestPath(request);

        ApiError apiError = apiErrorBuilder.build(
                errorCode,
                errorCode.getDefaultMessage(),
                path
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiError));
    }
}
