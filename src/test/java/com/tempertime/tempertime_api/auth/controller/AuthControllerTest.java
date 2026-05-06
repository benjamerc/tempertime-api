package com.tempertime.tempertime_api.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tempertime.tempertime_api.auth.dto.request.AuthLoginRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRefreshTokenRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRegisterRequest;
import com.tempertime.tempertime_api.auth.dto.response.AuthRegisterResponse;
import com.tempertime.tempertime_api.auth.dto.response.AuthTokenResponse;
import com.tempertime.tempertime_api.auth.exception.EmailAlreadyExistsException;
import com.tempertime.tempertime_api.auth.service.AuthService;
import com.tempertime.tempertime_api.common.error.ApiErrorBuilder;
import com.tempertime.tempertime_api.common.error.GlobalExceptionHandler;
import com.tempertime.tempertime_api.common.error.model.ErrorCode;
import com.tempertime.tempertime_api.common.validator.InvalidPasswordFormatException;
import com.tempertime.tempertime_api.security.exception.RefreshTokenExpiredException;
import com.tempertime.tempertime_api.security.exception.RefreshTokenNotFoundException;
import com.tempertime.tempertime_api.security.exception.RefreshTokenRevokedException;
import com.tempertime.tempertime_api.security.jwt.AccessTokenService;
import com.tempertime.tempertime_api.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, ApiErrorBuilder.class})
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @Nested
    class RegisterTests {

        @Test
        void shouldRegisterUser_whenValidRequest() throws Exception {

            AuthRegisterRequest request = new AuthRegisterRequest(
                    "john.doe@example.com", "John", "Doe", "Password123"
            );

            AuthRegisterResponse response = new AuthRegisterResponse("john.doe@example.com");

            when(authService.register(any())).thenReturn(response);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value(response.email()));

            verify(authService).register(any());
        }

        @Test
        void shouldReturn400_whenEmailIsMissing() throws Exception {

            AuthRegisterRequest request = new AuthRegisterRequest(
                    null, "John", "Doe", "Password123"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(authService, never()).register(any());
        }

        @Test
        void shouldReturn400_whenEmailFormatIsInvalid() throws Exception {

            AuthRegisterRequest request = new AuthRegisterRequest(
                    "notanemail", "John", "Doe", "Password123"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(authService, never()).register(any());
        }

        @Test
        void shouldReturn400_whenFirstNameIsTooShort() throws Exception {

            AuthRegisterRequest request = new AuthRegisterRequest(
                    "john.doe@example.com", "J", "Doe", "Password123"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(authService, never()).register(any());
        }

        @Test
        void shouldReturn400_whenLastNameIsTooShort() throws Exception {

            AuthRegisterRequest request = new AuthRegisterRequest(
                    "john.doe@example.com", "John", "D", "Password123"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(authService, never()).register(any());
        }

        @Test
        void shouldReturn400_whenPasswordIsTooShort() throws Exception {

            AuthRegisterRequest request = new AuthRegisterRequest(
                    "john.doe@example.com", "John", "Doe", "Pass1"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(authService, never()).register(any());
        }

        @Test
        void shouldReturn400_whenPasswordFormatIsInvalid() throws Exception {

            AuthRegisterRequest request = new AuthRegisterRequest(
                    "john.doe@example.com", "John", "Doe", "badpassword"
            );

            doThrow(new InvalidPasswordFormatException())
                    .when(authService).register(any());

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PASSWORD_FORMAT.name()));

            verify(authService).register(any());
        }

        @Test
        void shouldReturn409_whenEmailAlreadyExists() throws Exception {

            AuthRegisterRequest request = new AuthRegisterRequest(
                    "john.doe@example.com", "John", "Doe", "Password123"
            );

            doThrow(new EmailAlreadyExistsException())
                    .when(authService).register(any());

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCode.EMAIL_ALREADY_EXISTS.name()));

            verify(authService).register(any());
        }
    }

    @Nested
    class LoginTests {

        @Test
        void shouldReturnTokens_whenValidCredentials() throws Exception {

            AuthLoginRequest request = new AuthLoginRequest(
                    "john.doe@example.com", "Password123"
            );

            AuthTokenResponse response = new AuthTokenResponse("accessToken", "refreshToken");

            when(authService.login(any())).thenReturn(response);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(response.accessToken()))
                    .andExpect(jsonPath("$.refreshToken").value(response.refreshToken()));

            verify(authService).login(any());
        }

        @Test
        void shouldReturn400_whenEmailIsMissing() throws Exception {

            AuthLoginRequest request = new AuthLoginRequest(null, "Password123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(authService, never()).login(any());
        }

        @Test
        void shouldReturn400_whenPasswordIsMissing() throws Exception {

            AuthLoginRequest request = new AuthLoginRequest("john.doe@example.com", null);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(authService, never()).login(any());
        }

        @Test
        void shouldReturn401_whenCredentialsAreInvalid() throws Exception {

            AuthLoginRequest request = new AuthLoginRequest(
                    "john.doe@example.com", "WrongPassword"
            );

            doThrow(new BadCredentialsException("Bad credentials"))
                    .when(authService).login(any());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(ErrorCode.BAD_CREDENTIALS.name()));

            verify(authService).login(any());
        }
    }

    @Nested
    class RefreshTests {

        @Test
        void shouldReturnNewTokens_whenValidRefreshToken() throws Exception {

            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(UUID.randomUUID());
            AuthTokenResponse response = new AuthTokenResponse("newAccessToken", "newRefreshToken");

            when(authService.refresh(any())).thenReturn(response);

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(response.accessToken()))
                    .andExpect(jsonPath("$.refreshToken").value(response.refreshToken()));

            verify(authService).refresh(any());
        }

        @Test
        void shouldReturn400_whenRefreshTokenIsMissing() throws Exception {

            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(null);

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(authService, never()).refresh(any());
        }

        @Test
        void shouldReturn401_whenRefreshTokenDoesNotExist() throws Exception {

            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(UUID.randomUUID());

            doThrow(new RefreshTokenNotFoundException())
                    .when(authService).refresh(any());

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_NOT_FOUND.name()));

            verify(authService).refresh(any());
        }

        @Test
        void shouldReturn401_whenRefreshTokenIsExpired() throws Exception {

            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(UUID.randomUUID());

            doThrow(new RefreshTokenExpiredException())
                    .when(authService).refresh(any());

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_EXPIRED.name()));

            verify(authService).refresh(any());
        }

        @Test
        void shouldReturn403_whenRefreshTokenIsRevoked() throws Exception {

            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(UUID.randomUUID());

            doThrow(new RefreshTokenRevokedException())
                    .when(authService).refresh(any());

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_REVOKED.name()));

            verify(authService).refresh(any());
        }
    }

    @Nested
    class LogoutTests {

        @Test
        void shouldLogout_whenValidRefreshToken() throws Exception {

            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(UUID.randomUUID());

            doNothing().when(authService).logout(any());

            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            verify(authService).logout(any());
        }

        @Test
        void shouldReturn400_whenRefreshTokenIsMissing() throws Exception {

            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(null);

            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(authService, never()).logout(any());
        }

        @Test
        void shouldReturn401_whenRefreshTokenDoesNotExist() throws Exception {

            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(UUID.randomUUID());

            doThrow(new RefreshTokenNotFoundException())
                    .when(authService).logout(any());

            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_NOT_FOUND.name()));

            verify(authService).logout(any());
        }

        @Test
        void shouldReturn401_whenRefreshTokenIsExpired() throws Exception {

            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(UUID.randomUUID());

            doThrow(new RefreshTokenExpiredException())
                    .when(authService).logout(any());

            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_EXPIRED.name()));

            verify(authService).logout(any());
        }

        @Test
        void shouldReturn403_whenRefreshTokenIsRevoked() throws Exception {

            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(UUID.randomUUID());

            doThrow(new RefreshTokenRevokedException())
                    .when(authService).logout(any());

            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_REVOKED.name()));

            verify(authService).logout(any());
        }
    }
}