package com.tempertime.tempertime_api.workspaces.client;

import com.tempertime.tempertime_api.support.IntegrationTestSupport;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceCreateRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceJoinRequest;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceCreateResponse;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceJoinResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * Test client for interacting with the Workspace API in integration tests.
 */
@Component
public class WorkspaceTestClient {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private IntegrationTestSupport support;

    public WorkspaceCreateResponse createWorkspace(String token) {

        return restTemplate.exchange(
                "/api/workspaces",
                HttpMethod.POST,
                new HttpEntity<>(
                        new WorkspaceCreateRequest("Test Workspace", null),
                        support.bearerHeaders(token)
                ),
                WorkspaceCreateResponse.class
        ).getBody();
    }

    public WorkspaceJoinResponse joinWorkspace(String token, String inviteCode) {

        return restTemplate.exchange(
                "/api/workspaces/join",
                HttpMethod.POST,
                new HttpEntity<>(
                        new WorkspaceJoinRequest(inviteCode),
                        support.bearerHeaders(token)
                ),
                WorkspaceJoinResponse.class
        ).getBody();
    }
}
