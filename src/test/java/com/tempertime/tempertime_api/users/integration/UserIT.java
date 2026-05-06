package com.tempertime.tempertime_api.users.integration;

import com.tempertime.tempertime_api.auth.dto.request.AuthLoginRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRefreshTokenRequest;
import com.tempertime.tempertime_api.auth.dto.response.AuthTokenResponse;
import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import com.tempertime.tempertime_api.events.repository.EventUserRepository;
import com.tempertime.tempertime_api.security.refresh.RefreshTokenRepository;
import com.tempertime.tempertime_api.support.IntegrationTestHelper;
import com.tempertime.tempertime_api.users.dto.response.UserProfileResponse;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceCreateResponse;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceListItemResponse;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceInviteCodeRepository;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceRepository;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User E2E tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private IntegrationTestHelper testHelper;

    @Autowired
    private EventUserRepository eventUserRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private WorkspaceUserRepository workspaceUserRepository;
    @Autowired
    private WorkspaceInviteCodeRepository workspaceInviteCodeRepository;
    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        eventUserRepository.deleteAll();
        eventRepository.deleteAll();
        workspaceUserRepository.deleteAll();
        workspaceInviteCodeRepository.deleteAll();
        workspaceRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturn409_whenOwnerTriesToDeleteAccountWithOwnedWorkspace() {

        // Owner creates workspace
        AuthTokenResponse ownerTokens = testHelper.registerAndLogin("owner@mail.com", "Password123");
        WorkspaceCreateResponse workspace = testHelper.createWorkspace(ownerTokens.accessToken());

        assertThat(ownerTokens).isNotNull();
        assertThat(ownerTokens.accessToken()).isNotBlank();

        assertThat(workspace).isNotNull();
        assertThat(workspace.id()).isNotNull();

        // Owner tries to delete their account
        String deleteBody = """
            {
              "currentPassword": "Password123"
            }
            """;

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/users/me",
                HttpMethod.DELETE,
                new HttpEntity<>(deleteBody, testHelper.bearerHeaders(ownerTokens.accessToken())),
                Void.class
        );

        assertThat(deleteResponse).isNotNull();
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Verify that the user still exists
        assertThat(userRepository.existsByEmail("owner@mail.com")).isTrue();

        // Verify that the workspace still exists
        ResponseEntity<PageResponse<WorkspaceListItemResponse>> workspacesResponse = restTemplate.exchange(
                "/api/workspaces",
                HttpMethod.GET,
                new HttpEntity<>(testHelper.bearerHeaders(ownerTokens.accessToken())),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(workspacesResponse).isNotNull();
        assertThat(workspacesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(workspacesResponse.getBody()).isNotNull();
        assertThat(workspacesResponse.getBody().content()).isNotEmpty();
    }

    @Test
    void shouldRevokeAllTokens_whenPasswordIsChanged() {

        // Register and login
        AuthTokenResponse tokens = testHelper.registerAndLogin("user@mail.com", "Password123");

        assertThat(tokens).isNotNull();
        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();

        String originalAccessToken = tokens.accessToken();
        String originalRefreshToken = tokens.refreshToken();

        // Verify that the original access token works
        ResponseEntity<UserProfileResponse> beforeResponse = restTemplate.exchange(
                "/api/users/me",
                HttpMethod.GET,
                new HttpEntity<>(testHelper.bearerHeaders(originalAccessToken)),
                UserProfileResponse.class
        );

        assertThat(beforeResponse).isNotNull();
        assertThat(beforeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(beforeResponse.getBody()).isNotNull();

        // Change password
        String updatePasswordBody = """
            {
              "currentPassword": "Password123",
              "newPassword": "NewPassword456"
            }
            """;

        ResponseEntity<Void> updatePasswordResponse = restTemplate.exchange(
                "/api/users/me/password",
                HttpMethod.PATCH,
                new HttpEntity<>(updatePasswordBody, testHelper.bearerHeaders(originalAccessToken)),
                Void.class
        );

        assertThat(updatePasswordResponse).isNotNull();
        assertThat(updatePasswordResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify that the original refresh token was revoked
        AuthRefreshTokenRequest refreshRequest = new AuthRefreshTokenRequest(
                UUID.fromString(originalRefreshToken)
        );

        ResponseEntity<Void> refreshResponse = restTemplate.postForEntity(
                "/api/auth/refresh",
                refreshRequest,
                Void.class
        );

        assertThat(refreshResponse).isNotNull();
        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Verify that login works with the new password
        ResponseEntity<AuthTokenResponse> newLoginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                new AuthLoginRequest("user@mail.com", "NewPassword456"),
                AuthTokenResponse.class
        );

        assertThat(newLoginResponse).isNotNull();
        assertThat(newLoginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(newLoginResponse.getBody()).isNotNull();
        assertThat(newLoginResponse.getBody().accessToken()).isNotBlank();

        String newAccessToken = newLoginResponse.getBody().accessToken();

        // Verify that the new access token works
        ResponseEntity<UserProfileResponse> afterResponse = restTemplate.exchange(
                "/api/users/me",
                HttpMethod.GET,
                new HttpEntity<>(testHelper.bearerHeaders(newAccessToken)),
                UserProfileResponse.class
        );

        assertThat(afterResponse).isNotNull();
        assertThat(afterResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(afterResponse.getBody()).isNotNull();
        assertThat(afterResponse.getBody().email()).isEqualTo("user@mail.com");
    }
}
