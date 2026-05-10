package com.tempertime.tempertime_api.events.client;

import com.tempertime.tempertime_api.events.domain.EventScope;
import com.tempertime.tempertime_api.events.dto.response.EventCreateResponse;
import com.tempertime.tempertime_api.support.IntegrationTestSupport;
import com.tempertime.tempertime_api.events.support.EventTestDateFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * Test client for interacting with the Event API in integration tests.
 */
@Component
public class EventTestClient {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private IntegrationTestSupport support;

    public EventCreateResponse createEvent(String token, Long workspaceId, EventScope scope) {

        String body = """
                {
                  "title": "Test Event",
                  "eventDate": "%s",
                  "description": "Test Description",
                  "scope": "%s",
                  "color": "#A3B4C5"
                }
                """
                .formatted(
                        EventTestDateFactory.futureDate(),
                        scope.name()
                );

        return restTemplate.exchange(
                "/api/workspaces/" + workspaceId + "/events",
                HttpMethod.POST,
                new HttpEntity<>(body, support.bearerHeaders(token)),
                EventCreateResponse.class
        ).getBody();
    }
}
