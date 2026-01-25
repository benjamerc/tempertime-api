package com.tempertime.tempertime_api.security.jwt;

import com.tempertime.tempertime_api.security.core.CustomUserDetails;
import com.tempertime.tempertime_api.users.model.User;
import com.tempertime.tempertime_api.users.model.UserRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Extracts and validates JWT access tokens from requests */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AccessTokenService accessTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Extract Authorization header
        String authHeader = request.getHeader("Authorization");

        // Skip if no Bearer token present
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // Validate the token and extract claims
        Claims claims;
        try {
            claims = accessTokenService.validateAccessToken(token);
        } catch (Exception ex) {
            log.warn("Invalid or expired JWT token: {}", ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Build User from token claims
        String email = claims.getSubject();
        Long userId = claims.get("id", Long.class);
        String role = claims.get("role", String.class);

        User userFromToken = User.builder()
                .id(userId)
                .email(email)
                .role(UserRole.valueOf(role))
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(userFromToken);

        // Set authentication if not already present
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
