package com.tempertime.tempertime_api.common.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestPathResolver {

    /**
     * Returns the request path for error responses.
     */
    public static String resolve(HttpServletRequest request) {
        Object pathAttr = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        return pathAttr != null ? pathAttr.toString() : request.getRequestURI();
    }
}
