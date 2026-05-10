package com.tempertime.tempertime_api.events.integration;

import com.tempertime.tempertime_api.auth.dto.response.AuthTokenResponse;
import com.tempertime.tempertime_api.events.domain.EventScope;
import com.tempertime.tempertime_api.events.dto.response.EventAssignUserResponse;
import com.tempertime.tempertime_api.events.dto.response.EventCreateResponse;
import com.tempertime.tempertime_api.events.dto.response.EventResponse;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import com.tempertime.tempertime_api.events.repository.EventUserRepository;
import com.tempertime.tempertime_api.security.refresh.RefreshTokenRepository;
import com.tempertime.tempertime_api.support.IntegrationTestSupport;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceCreateResponse;
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
 * Event E2E tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class EventIT {

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
    void shouldAssignGlobalEventToAllWorkspaceMembers() {

        // Owner creates workspace
        AuthTokenResponse ownerTokens = integrationTestSupport.registerAndLogin("owner@mail.com", "Password123");
        assertThat(ownerTokens).isNotNull();

        WorkspaceCreateResponse workspace = integrationTestSupport.createWorkspace(ownerTokens.accessToken());
        assertThat(workspace).isNotNull();
        assertThat(workspace.id()).isNotNull();
        assertThat(workspace.inviteCode()).isNotBlank();

        // Member joins
        AuthTokenResponse memberTokens = integrationTestSupport.registerAndLogin("member@mail.com", "Password123");
        assertThat(memberTokens).isNotNull();

        integrationTestSupport.joinWorkspace(memberTokens.accessToken(), workspace.inviteCode());

        // Owner creates GLOBAL event
        EventCreateResponse event = integrationTestSupport.createEvent(
                ownerTokens.accessToken(),
                workspace.id(),
                EventScope.GLOBAL
        );

        assertThat(event).isNotNull();
        assertThat(event.id()).isNotNull();

        String eventUrl = "/api/workspaces/" + workspace.id() + "/events/" + event.id();

        // Owner can see event
        ResponseEntity<EventResponse> ownerEventResponse = restTemplate.exchange(
                eventUrl,
                HttpMethod.GET,
                new HttpEntity<>(integrationTestSupport.bearerHeaders(ownerTokens.accessToken())),
                EventResponse.class
        );

        assertThat(ownerEventResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        EventResponse ownerBody = ownerEventResponse.getBody();
        assertThat(ownerBody).isNotNull();
        assertThat(ownerBody.scope()).isEqualTo(EventScope.GLOBAL.name());

        // Member can see event
        ResponseEntity<EventResponse> memberEventResponse = restTemplate.exchange(
                eventUrl,
                HttpMethod.GET,
                new HttpEntity<>(integrationTestSupport.bearerHeaders(memberTokens.accessToken())),
                EventResponse.class
        );

        assertThat(memberEventResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        EventResponse memberBody = memberEventResponse.getBody();
        assertThat(memberBody).isNotNull();
        assertThat(memberBody.hasActiveUsers()).isTrue();
    }

    @Test
    void shouldAssignUsersToSpecificEvent() {

        // Owner creates workspace
        AuthTokenResponse ownerTokens = integrationTestSupport.registerAndLogin("owner@mail.com", "Password123");
        assertThat(ownerTokens).isNotNull();

        WorkspaceCreateResponse workspace = integrationTestSupport.createWorkspace(ownerTokens.accessToken());
        assertThat(workspace).isNotNull();
        assertThat(workspace.id()).isNotNull();
        assertThat(workspace.inviteCode()).isNotBlank();

        // Member joins
        AuthTokenResponse memberTokens = integrationTestSupport.registerAndLogin("member@mail.com", "Password123");
        assertThat(memberTokens).isNotNull();

        WorkspaceJoinResponse joinResponse = integrationTestSupport.joinWorkspace(memberTokens.accessToken(), workspace.inviteCode());
        assertThat(joinResponse).isNotNull();
        assertThat(joinResponse.userId()).isNotNull();

        // Owner creates SPECIFIC event
        EventCreateResponse event = integrationTestSupport.createEvent(
                ownerTokens.accessToken(),
                workspace.id(),
                EventScope.SPECIFIC
        );

        assertThat(event).isNotNull();
        assertThat(event.id()).isNotNull();

        String eventUrl = "/api/workspaces/" + workspace.id() + "/events/" + event.id();
        String assignUsersUrl = eventUrl + "/users";

        // Member cannot see event yet
        ResponseEntity<Void> beforeAssignResponse = restTemplate.exchange(
                eventUrl,
                HttpMethod.GET,
                new HttpEntity<>(integrationTestSupport.bearerHeaders(memberTokens.accessToken())),
                Void.class
        );

        assertThat(beforeAssignResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Owner assigns member to event
        String assignBody = """
            {
              "userIds": [%d]
            }
            """.formatted(joinResponse.userId());

        ResponseEntity<EventAssignUserResponse> assignResponse = restTemplate.exchange(
                assignUsersUrl,
                HttpMethod.POST,
                new HttpEntity<>(assignBody, integrationTestSupport.bearerHeaders(ownerTokens.accessToken())),
                EventAssignUserResponse.class
        );

        assertThat(assignResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        EventAssignUserResponse assignBodyResponse = assignResponse.getBody();
        assertThat(assignBodyResponse).isNotNull();
        assertThat(assignBodyResponse.userIds()).contains(joinResponse.userId());

        // Member now can see event
        ResponseEntity<EventResponse> afterAssignResponse = restTemplate.exchange(
                eventUrl,
                HttpMethod.GET,
                new HttpEntity<>(integrationTestSupport.bearerHeaders(memberTokens.accessToken())),
                EventResponse.class
        );

        assertThat(afterAssignResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        EventResponse afterBody = afterAssignResponse.getBody();
        assertThat(afterBody).isNotNull();
        assertThat(afterBody.hasActiveUsers()).isTrue();
    }

    @Test
    void shouldLoseEventAccess_whenUserIsRemovedFromWorkspace() {

        // Owner creates workspace
        AuthTokenResponse ownerTokens = integrationTestSupport.registerAndLogin("owner@mail.com", "Password123");
        WorkspaceCreateResponse workspace = integrationTestSupport.createWorkspace(ownerTokens.accessToken());

        // Member joins
        AuthTokenResponse memberTokens = integrationTestSupport.registerAndLogin("member@mail.com", "Password123");
        WorkspaceJoinResponse joinResponse = integrationTestSupport.joinWorkspace(memberTokens.accessToken(), workspace.inviteCode());

        // Owner creates GLOBAL event - member is automatically assigned
        EventCreateResponse event = integrationTestSupport.createEvent(ownerTokens.accessToken(), workspace.id(), EventScope.GLOBAL);

        // Verify that member has access to the event
        ResponseEntity<EventResponse> beforeRemoveResponse = restTemplate.exchange(
                "/api/workspaces/" + workspace.id() + "/events/" + event.id(),
                HttpMethod.GET,
                new HttpEntity<>(integrationTestSupport.bearerHeaders(memberTokens.accessToken())),
                EventResponse.class
        );

        assertThat(beforeRemoveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Owner removes member from workspace
        ResponseEntity<Void> removeResponse = restTemplate.exchange(
                "/api/workspaces/" + workspace.id() + "/users/" + joinResponse.userId(),
                HttpMethod.DELETE,
                new HttpEntity<>(integrationTestSupport.bearerHeaders(ownerTokens.accessToken())),
                Void.class
        );

        assertThat(removeResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Member can no longer access the workspace
        ResponseEntity<Void> workspaceResponse = restTemplate.exchange(
                "/api/workspaces/" + workspace.id(),
                HttpMethod.GET,
                new HttpEntity<>(integrationTestSupport.bearerHeaders(memberTokens.accessToken())),
                Void.class
        );

        assertThat(workspaceResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Member is no longer assigned to the event
        boolean stillAssigned = eventUserRepository
                .existsByEventIdAndUserId(event.id(), joinResponse.userId());

        assertThat(stillAssigned).isFalse();
    }

    @Test
    void shouldCleanupAllData_whenUserDeletesAccount() {

        // Owner creates workspace
        AuthTokenResponse ownerTokens = integrationTestSupport.registerAndLogin("owner@mail.com", "Password123");
        assertThat(ownerTokens).isNotNull();

        WorkspaceCreateResponse workspace = integrationTestSupport.createWorkspace(ownerTokens.accessToken());
        assertThat(workspace).isNotNull();
        assertThat(workspace.id()).isNotNull();

        // Member joins
        AuthTokenResponse memberTokens = integrationTestSupport.registerAndLogin("member@mail.com", "Password123");
        assertThat(memberTokens).isNotNull();

        WorkspaceJoinResponse joinResponse = integrationTestSupport.joinWorkspace(memberTokens.accessToken(), workspace.inviteCode());
        assertThat(joinResponse).isNotNull();
        assertThat(joinResponse.userId()).isNotNull();

        // Owner creates GLOBAL event
        EventCreateResponse event = integrationTestSupport.createEvent(
                ownerTokens.accessToken(),
                workspace.id(),
                EventScope.GLOBAL
        );

        assertThat(event).isNotNull();
        assertThat(event.id()).isNotNull();

        // Verify that member is assigned to the event
        assertThat(eventUserRepository.existsByEventIdAndUserId(event.id(), joinResponse.userId())).isTrue();

        // Member deletes account
        String deleteBody = """
            {
              "currentPassword": "Password123"
            }
            """;

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/users/me",
                HttpMethod.DELETE,
                new HttpEntity<>(deleteBody, integrationTestSupport.bearerHeaders(memberTokens.accessToken())),
                Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify user removed
        assertThat(userRepository.existsByEmail("member@mail.com")).isFalse();

        // Verify event assignments removed
        assertThat(eventUserRepository.existsByEventIdAndUserId(event.id(), joinResponse.userId())).isFalse();

        // Verify workspace membership removed
        assertThat(workspaceUserRepository.existsByWorkspaceIdAndUserId(workspace.id(), joinResponse.userId())).isFalse();
    }
}
