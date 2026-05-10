package com.tempertime.tempertime_api.workspaces.integration;

import com.tempertime.tempertime_api.auth.dto.response.AuthTokenResponse;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import com.tempertime.tempertime_api.events.repository.EventUserRepository;
import com.tempertime.tempertime_api.security.refresh.RefreshTokenRepository;
import com.tempertime.tempertime_api.support.IntegrationTestSupport;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceJoinRequest;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceCreateResponse;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceDetailResponse;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceJoinResponse;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceInviteCodeRepository;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceRepository;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceUserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Workspace E2E tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WorkspaceIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private IntegrationTestSupport integrationTestSupport;

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
    void shouldCreateWorkspaceJoinWithInviteCodeAndVerifyMembership() {

        // Owner creates workspace
        AuthTokenResponse ownerTokens = integrationTestSupport.registerAndLogin("owner@mail.com", "Password123");
        WorkspaceCreateResponse workspace = integrationTestSupport.createWorkspace(ownerTokens.accessToken());

        assertThat(workspace).isNotNull();
        assertThat(workspace.id()).isNotNull();
        assertThat(workspace.inviteCode()).isNotBlank();

        // Member registers
        AuthTokenResponse memberTokens = integrationTestSupport.registerAndLogin("member@mail.com", "Password123");

        // Member joins with invite code
        WorkspaceJoinRequest joinRequest = new WorkspaceJoinRequest(workspace.inviteCode());

        ResponseEntity<WorkspaceJoinResponse> joinResponse = restTemplate.exchange(
                "/api/workspaces/join",
                HttpMethod.POST,
                new HttpEntity<>(joinRequest, integrationTestSupport.bearerHeaders(memberTokens.accessToken())),
                WorkspaceJoinResponse.class
        );

        assertThat(joinResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(joinResponse.getBody()).isNotNull();
        assertThat(joinResponse.getBody().workspaceId()).isEqualTo(workspace.id());
        assertThat(joinResponse.getBody().role()).isEqualTo(WorkspaceRole.MEMBER);

        // Verify that the member can see the workspace
        ResponseEntity<WorkspaceDetailResponse> detailResponse = restTemplate.exchange(
                "/api/workspaces/" + workspace.id(),
                HttpMethod.GET,
                new HttpEntity<>(integrationTestSupport.bearerHeaders(memberTokens.accessToken())),
                WorkspaceDetailResponse.class
        );

        assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(detailResponse.getBody()).isNotNull();
        assertThat(detailResponse.getBody().userRole()).isEqualTo(WorkspaceRole.MEMBER);
    }

    @Test
    void shouldReturn409_whenDeletingWorkspaceWithoutArchivingFirst() {

        AuthTokenResponse ownerTokens = integrationTestSupport.registerAndLogin("owner@mail.com", "Password123");
        WorkspaceCreateResponse workspace = integrationTestSupport.createWorkspace(ownerTokens.accessToken());

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/workspaces/" + workspace.id(),
                HttpMethod.DELETE,
                new HttpEntity<>(integrationTestSupport.bearerHeaders(ownerTokens.accessToken())),
                Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Verify that the workspace still exists
        ResponseEntity<WorkspaceDetailResponse> detailResponse = restTemplate.exchange(
                "/api/workspaces/" + workspace.id(),
                HttpMethod.GET,
                new HttpEntity<>(integrationTestSupport.bearerHeaders(ownerTokens.accessToken())),
                WorkspaceDetailResponse.class
        );

        assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturn403_whenOwnerTriesToLeaveWorkspace() {

        AuthTokenResponse ownerTokens = integrationTestSupport.registerAndLogin("owner@mail.com", "Password123");
        WorkspaceCreateResponse workspace = integrationTestSupport.createWorkspace(ownerTokens.accessToken());

        ResponseEntity<Void> leaveResponse = restTemplate.exchange(
                "/api/workspaces/" + workspace.id() + "/users/me",
                HttpMethod.DELETE,
                new HttpEntity<>(integrationTestSupport.bearerHeaders(ownerTokens.accessToken())),
                Void.class
        );

        assertThat(leaveResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Verify that the owner is still in the workspace
        ResponseEntity<WorkspaceDetailResponse> detailResponse = restTemplate.exchange(
                "/api/workspaces/" + workspace.id(),
                HttpMethod.GET,
                new HttpEntity<>(integrationTestSupport.bearerHeaders(ownerTokens.accessToken())),
                WorkspaceDetailResponse.class
        );

        assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        WorkspaceDetailResponse body = detailResponse.getBody();
        assertThat(body).isNotNull();
        assertThat(body.userRole()).isEqualTo(WorkspaceRole.OWNER);
    }

    @Test
    void shouldLeaveWorkspace_whenUserIsMember() {

        // Owner creates workspace
        AuthTokenResponse ownerTokens = integrationTestSupport.registerAndLogin("owner@mail.com", "Password123");
        WorkspaceCreateResponse workspace = integrationTestSupport.createWorkspace(ownerTokens.accessToken());

        // Member joins
        AuthTokenResponse memberTokens = integrationTestSupport.registerAndLogin("member@mail.com", "Password123");
        WorkspaceJoinRequest joinRequest = new WorkspaceJoinRequest(workspace.inviteCode());

        restTemplate.exchange(
                "/api/workspaces/join",
                HttpMethod.POST,
                new HttpEntity<>(joinRequest, integrationTestSupport.bearerHeaders(memberTokens.accessToken())),
                WorkspaceJoinResponse.class
        );

        // Member leaves workspace
        ResponseEntity<Void> leaveResponse = restTemplate.exchange(
                "/api/workspaces/" + workspace.id() + "/users/me",
                HttpMethod.DELETE,
                new HttpEntity<>(integrationTestSupport.bearerHeaders(memberTokens.accessToken())),
                Void.class
        );

        assertThat(leaveResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify that the member can no longer see the workspace
        ResponseEntity<Void> detailResponse = restTemplate.exchange(
                "/api/workspaces/" + workspace.id(),
                HttpMethod.GET,
                new HttpEntity<>(integrationTestSupport.bearerHeaders(memberTokens.accessToken())),
                Void.class
        );

        assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}