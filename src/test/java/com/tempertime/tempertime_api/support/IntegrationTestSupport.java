package com.tempertime.tempertime_api.support;

import com.tempertime.tempertime_api.auth.dto.request.AuthLoginRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRegisterRequest;
import com.tempertime.tempertime_api.auth.dto.response.AuthRegisterResponse;
import com.tempertime.tempertime_api.auth.dto.response.AuthTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Integration test support for authentication and HTTP configuration.
 */
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
}