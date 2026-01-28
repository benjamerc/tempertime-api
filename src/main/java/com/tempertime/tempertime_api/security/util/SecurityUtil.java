package com.tempertime.tempertime_api.security.util;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityUtil {

    /** Returns the request path for error responses */
    public static String resolveRequestPath(HttpServletRequest request) {
        Object pathAttr = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        return pathAttr != null ? pathAttr.toString() : request.getRequestURI();
    }
}
