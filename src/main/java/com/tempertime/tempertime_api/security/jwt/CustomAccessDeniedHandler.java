package com.tempertime.tempertime_api.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tempertime.tempertime_api.common.error.ApiErrorBuilder;
import com.tempertime.tempertime_api.common.error.model.ApiError;
import com.tempertime.tempertime_api.common.error.model.ErrorCode;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Returns a standardized 403 response when a user lacks permission for a request. */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final ApiErrorBuilder apiErrorBuilder;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex
    ) throws IOException {

        String path = (String) request.getAttribute(
                RequestDispatcher.ERROR_REQUEST_URI
        );

        if (path == null) {
            path = request.getRequestURI();
        }

        ApiError apiError = apiErrorBuilder.build(
                ErrorCode.ACCESS_DENIED,
                "You do not have permission to access this resource",
                path
        );

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        response.getWriter().write(
                objectMapper.writeValueAsString(apiError)
        );
    }
}
