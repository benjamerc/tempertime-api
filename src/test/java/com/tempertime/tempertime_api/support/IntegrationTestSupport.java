package com.tempertime.tempertime_api.support;

import com.tempertime.tempertime_api.auth.dto.request.AuthLoginRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRegisterRequest;
import com.tempertime.tempertime_api.auth.dto.response.AuthRegisterResponse;
import com.tempertime.tempertime_api.auth.dto.response.AuthTokenResponse;
import com.tempertime.tempertime_api.events.domain.EventScope;
import com.tempertime.tempertime_api.events.dto.response.EventCreateResponse;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceCreateRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceJoinRequest;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceCreateResponse;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceJoinResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class IntegrationTestSupport {

    @Autowired
    private TestRestTemplate restTemplate;

    public AuthTokenResponse registerAndLogin(String email, String password) {

        restTemplate.postForEntity(
                "/api/auth/register",
                new AuthRegisterRequest(email, "John", "Doe", password),
                AuthRegisterResponse.class
        );

        ResponseEntity<AuthTokenResponse> loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                new AuthLoginRequest(email, password),
                AuthTokenResponse.class
        );

        return loginResponse.getBody();
    }

    public HttpHeaders bearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public WorkspaceCreateResponse createWorkspace(String accessToken) {

        ResponseEntity<WorkspaceCreateResponse> response = restTemplate.exchange(
                "/api/workspaces",
                HttpMethod.POST,
                new HttpEntity<>(new WorkspaceCreateRequest("Test Workspace", null), bearerHeaders(accessToken)),
                WorkspaceCreateResponse.class
        );

        return response.getBody();
    }

    public WorkspaceJoinResponse joinWorkspace(String accessToken, String inviteCode) {

        ResponseEntity<WorkspaceJoinResponse> response = restTemplate.exchange(
                "/api/workspaces/join",
                HttpMethod.POST,
                new HttpEntity<>(new WorkspaceJoinRequest(inviteCode), bearerHeaders(accessToken)),
                WorkspaceJoinResponse.class
        );

        return response.getBody();
    }

    public EventCreateResponse createEvent(String accessToken, Long workspaceId, EventScope scope) {

        String body = """
                {
                  "title": "Test Event",
                  "eventDate": "2027-05-02T10:30-03:00",
                  "description": "Test Description",
                  "scope": "%s",
                  "color": "#A3B4C5"
                }
                """.formatted(scope.name());

        ResponseEntity<EventCreateResponse> response = restTemplate.exchange(
                "/api/workspaces/" + workspaceId + "/events",
                HttpMethod.POST,
                new HttpEntity<>(body, bearerHeaders(accessToken)),
                EventCreateResponse.class
        );

        return response.getBody();
    }
}