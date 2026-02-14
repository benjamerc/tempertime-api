package com.tempertime.tempertime_api.security.jwt;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;
import com.tempertime.tempertime_api.security.core.CustomUserDetails;
import com.tempertime.tempertime_api.security.exception.AccessTokenExpiredException;
import com.tempertime.tempertime_api.security.exception.AccessTokenInvalidException;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
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

/**
 * Spring Security filter that extracts JWT access tokens from the Authorization header,
 * validates them, and sets the authentication in the SecurityContext.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AccessTokenService accessTokenService;

    // Claim names as constants
    private static final String CLAIM_ID = "id";
    private static final String CLAIM_ROLE = "role";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
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
        }
        catch (ExpiredJwtException ex) {
            // Mark request as having an expired token
            SecurityContextHolder.clearContext();
            request.setAttribute(SecurityRequestAttributes.SECURITY_ERROR_CODE.name(),
                    ErrorCode.ACCESS_TOKEN_EXPIRED);
            throw new AccessTokenExpiredException(ex);
        }
        catch (JwtException | IllegalArgumentException ex) {
            // Mark request as having an invalid token
            SecurityContextHolder.clearContext();
            request.setAttribute(SecurityRequestAttributes.SECURITY_ERROR_CODE.name(),
                    ErrorCode.ACCESS_TOKEN_INVALID);
            throw new AccessTokenInvalidException(ex);
        }

        // Build CustomUserDetails from token claims
        CustomUserDetails userDetails = buildUserFromClaims(claims);

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

    /**
     * Constructs a CustomUserDetails object from JWT claims.
     * Validates essential claims are present.
     */
    private CustomUserDetails buildUserFromClaims(Claims claims) {

        String email = claims.getSubject();
        Long userId = claims.get(CLAIM_ID, Long.class);
        String roleStr = claims.get(CLAIM_ROLE, String.class);

        // Validate required claims
        if (email == null || userId == null || roleStr == null) {
            throw new AccessTokenInvalidException(null);
        }

        UserRole role;
        try {
            role = UserRole.valueOf(roleStr);
        } catch (IllegalArgumentException ex) {
            throw new AccessTokenInvalidException(ex);
        }

        User user = User.builder()
                .id(userId)
                .email(email)
                .role(role)
                .build();

        return new CustomUserDetails(user);
    }
}
