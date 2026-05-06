package com.tempertime.tempertime_api.auth.integration;

import com.tempertime.tempertime_api.auth.dto.request.AuthLoginRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRefreshTokenRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRegisterRequest;
import com.tempertime.tempertime_api.auth.dto.response.AuthRegisterResponse;
import com.tempertime.tempertime_api.auth.dto.response.AuthTokenResponse;
import com.tempertime.tempertime_api.security.refresh.RefreshTokenRepository;
import com.tempertime.tempertime_api.support.IntegrationTestHelper;
import com.tempertime.tempertime_api.users.dto.response.UserProfileResponse;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Auth E2E tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private IntegrationTestHelper testHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterLoginAndAccessProtectedEndpoint() {

        // Register
        AuthRegisterRequest registerRequest = new AuthRegisterRequest(
                "john.doe@example.com", "John", "Doe", "Password123"
        );

        ResponseEntity<AuthRegisterResponse> registerResponse = restTemplate.postForEntity(
                "/api/auth/register",
                registerRequest,
                AuthRegisterResponse.class
        );

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerResponse.getBody()).isNotNull();
        assertThat(registerResponse.getBody().email()).isEqualTo("john.doe@example.com");

        // Login
        AuthLoginRequest loginRequest = new AuthLoginRequest(
                "john.doe@example.com", "Password123"
        );

        ResponseEntity<AuthTokenResponse> loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                AuthTokenResponse.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();

        String accessToken = loginResponse.getBody().accessToken();
        assertThat(accessToken).isNotBlank();

        // Access to protected endpoint
        HttpEntity<Void> request = new HttpEntity<>(testHelper.bearerHeaders(accessToken));

        ResponseEntity<UserProfileResponse> profileResponse = restTemplate.exchange(
                "/api/users/me",
                HttpMethod.GET,
                request,
                UserProfileResponse.class
        );

        assertThat(profileResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(profileResponse.getBody()).isNotNull();
        assertThat(profileResponse.getBody().email()).isEqualTo("john.doe@example.com");
        assertThat(profileResponse.getBody().firstName()).isEqualTo("John");
        assertThat(profileResponse.getBody().lastName()).isEqualTo("Doe");
    }

    @Test
    void shouldReturn401_whenLoginWithInvalidCredentials() {

        // Previous register
        AuthRegisterRequest registerRequest = new AuthRegisterRequest(
                "john.doe@example.com", "John", "Doe", "Password123"
        );

        restTemplate.postForEntity(
                "/api/auth/register",
                registerRequest,
                AuthRegisterResponse.class
        );

        // Login with wrong password
        AuthLoginRequest loginRequest = new AuthLoginRequest(
                "john.doe@example.com", "WrongPassword"
        );

        ResponseEntity<Void> loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                Void.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRotateRefreshToken_whenValidRefreshTokenProvided() {

        // Register
        AuthRegisterRequest registerRequest = new AuthRegisterRequest(
                "john.doe@example.com", "John", "Doe", "Password123"
        );

        restTemplate.postForEntity(
                "/api/auth/register",
                registerRequest,
                AuthRegisterResponse.class
        );

        // Login
        AuthLoginRequest loginRequest = new AuthLoginRequest(
                "john.doe@example.com", "Password123"
        );

        ResponseEntity<AuthTokenResponse> loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                AuthTokenResponse.class
        );

        AuthTokenResponse loginBody = loginResponse.getBody();
        assertThat(loginBody).isNotNull();

        String originalRefreshToken = loginBody.refreshToken();
        assertThat(originalRefreshToken).isNotBlank();

        // Refresh - get new tokens
        AuthRefreshTokenRequest refreshRequest = new AuthRefreshTokenRequest(
                UUID.fromString(originalRefreshToken)
        );

        ResponseEntity<AuthTokenResponse> refreshResponse = restTemplate.postForEntity(
                "/api/auth/refresh",
                refreshRequest,
                AuthTokenResponse.class
        );

        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        AuthTokenResponse refreshBody = refreshResponse.getBody();
        assertThat(refreshBody).isNotNull();

        String newAccessToken = refreshBody.accessToken();
        String newRefreshToken = refreshBody.refreshToken();

        assertThat(newAccessToken).isNotBlank();
        assertThat(newRefreshToken).isNotBlank();
        assertThat(newRefreshToken).isNotEqualTo(originalRefreshToken);

        // Verify that the original token no longer works
        AuthRefreshTokenRequest oldTokenRequest = new AuthRefreshTokenRequest(
                UUID.fromString(originalRefreshToken)
        );

        ResponseEntity<Void> reuseResponse = restTemplate.postForEntity(
                "/api/auth/refresh",
                oldTokenRequest,
                Void.class
        );

        assertThat(reuseResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Verify that the new access token works
        HttpEntity<Void> protectedRequest = new HttpEntity<>(testHelper.bearerHeaders(newAccessToken));

        ResponseEntity<UserProfileResponse> profileResponse = restTemplate.exchange(
                "/api/users/me",
                HttpMethod.GET,
                protectedRequest,
                UserProfileResponse.class
        );

        assertThat(profileResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(profileResponse.getBody()).isNotNull();
        assertThat(profileResponse.getBody().email()).isEqualTo("john.doe@example.com");
    }
}
