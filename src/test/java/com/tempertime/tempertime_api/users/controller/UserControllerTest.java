package com.tempertime.tempertime_api.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tempertime.tempertime_api.common.error.ApiErrorBuilder;
import com.tempertime.tempertime_api.common.error.GlobalExceptionHandler;
import com.tempertime.tempertime_api.common.error.model.ErrorCode;
import com.tempertime.tempertime_api.common.validator.InvalidPasswordFormatException;
import com.tempertime.tempertime_api.security.core.CurrentUserProvider;
import com.tempertime.tempertime_api.security.jwt.AccessTokenService;
import com.tempertime.tempertime_api.security.jwt.JwtAuthenticationFilter;
import com.tempertime.tempertime_api.users.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.dto.request.UserDeleteAccountRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdatePasswordRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdateProfileRequest;
import com.tempertime.tempertime_api.users.dto.response.UserProfileResponse;
import com.tempertime.tempertime_api.users.exception.InvalidPasswordException;
import com.tempertime.tempertime_api.users.service.core.UserService;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceOwnerExistsException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({GlobalExceptionHandler.class, ApiErrorBuilder.class})
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @Nested
    class UserProfileTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnUserProfile_whenUserIsAuthenticated() throws Exception {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);
            UserProfileResponse response = UserTestDataProvider.userProfileResponse(user);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(userService.getProfile(userId)).thenReturn(response);

            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.email").value(response.email()))
                    .andExpect(jsonPath("$.firstName").value(response.firstName()))
                    .andExpect(jsonPath("$.lastName").value(response.lastName()))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty());

            verify(currentUserProvider).getUserId();
            verify(userService).getProfile(userId);
        }
    }

    @Nested
    class UpdateProfileTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldUpdateProfile_whenValidRequest() throws Exception {

            Long userId = 1L;

            UserUpdateProfileRequest request =
                    new UserUpdateProfileRequest("FirstUpdated", "LastUpdated");

            User user = UserTestDataProvider.user(userId);
            user.setFirstName(request.firstName());
            user.setLastName(request.lastName());

            UserProfileResponse response =
                    UserTestDataProvider.userProfileResponse(user);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(userService.updateProfile(eq(userId), any()))
                    .thenReturn(response);

            mockMvc.perform(patch("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(response.email()))
                    .andExpect(jsonPath("$.firstName").value(request.firstName()))
                    .andExpect(jsonPath("$.lastName").value(request.lastName()))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty());

            verify(currentUserProvider).getUserId();
            verify(userService).updateProfile(eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenFirstNameIsTooShort() throws Exception {

            UserUpdateProfileRequest request =
                    new UserUpdateProfileRequest("F", "LastUpdated");

            mockMvc.perform(patch("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code")
                            .value(ErrorCode.VALIDATION_ERROR.name()));

            verify(currentUserProvider, never()).getUserId();
            verify(userService, never()).updateProfile(any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldUpdateOnlyFirstName_whenLastNameIsNotProvided() throws Exception {

            Long userId = 1L;

            UserUpdateProfileRequest request =
                    new UserUpdateProfileRequest("FirstUpdated", null);

            User user = UserTestDataProvider.user(userId);
            String originalLastName = user.getLastName();
            user.setFirstName(request.firstName());

            UserProfileResponse response =
                    UserTestDataProvider.userProfileResponse(user);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(userService.updateProfile(eq(userId), any()))
                    .thenReturn(response);

            mockMvc.perform(patch("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value(request.firstName()))
                    .andExpect(jsonPath("$.lastName").value(originalLastName));


            verify(currentUserProvider).getUserId();
            verify(userService).updateProfile(eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenLastNameIsTooShort() throws Exception {

            UserUpdateProfileRequest request =
                    new UserUpdateProfileRequest("FirstUpdated", "L");

            mockMvc.perform(patch("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code")
                            .value(ErrorCode.VALIDATION_ERROR.name()));

            verify(currentUserProvider, never()).getUserId();
            verify(userService, never()).updateProfile(any(), any());
        }
    }

    @Nested
    class UpdatePasswordTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldUpdatePassword_whenValidRequest() throws Exception {

            Long userId = 1L;

            UserUpdatePasswordRequest request =
                    new UserUpdatePasswordRequest(UserTestDataProvider.PASSWORD, "NewPassword123");

            when(currentUserProvider.getUserId()).thenReturn(userId);

            mockMvc.perform(patch("/api/users/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            verify(currentUserProvider).getUserId();
            verify(userService).updatePassword(eq(userId), eq(request));
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenCurrentPasswordIsIncorrect() throws Exception {

            Long userId = 1L;

            UserUpdatePasswordRequest request =
                    new UserUpdatePasswordRequest("WrongPassword", "NewPassword123");

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new InvalidPasswordException())
                    .when(userService).updatePassword(eq(userId), any());

            mockMvc.perform(patch("/api/users/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code")
                            .value(ErrorCode.INVALID_PASSWORD.name()));

            verify(currentUserProvider).getUserId();
            verify(userService).updatePassword(eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenNewPasswordIsTooShort() throws Exception {

            UserUpdatePasswordRequest request =
                    new UserUpdatePasswordRequest(UserTestDataProvider.PASSWORD, "123");

            mockMvc.perform(patch("/api/users/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code")
                            .value(ErrorCode.VALIDATION_ERROR.name()));

            verify(userService, never()).updatePassword(any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenPasswordFormatIsInvalid() throws Exception {

            Long userId = 1L;

            UserUpdatePasswordRequest request =
                    new UserUpdatePasswordRequest(UserTestDataProvider.PASSWORD, "password");

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new InvalidPasswordFormatException())
                    .when(userService).updatePassword(eq(userId), any());

            mockMvc.perform(patch("/api/users/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code")
                            .value(ErrorCode.INVALID_PASSWORD_FORMAT.name()));

            verify(userService).updatePassword(eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenCurrentPasswordIsMissing() throws Exception {

            UserUpdatePasswordRequest request =
                    new UserUpdatePasswordRequest("", "NewPassword123");

            mockMvc.perform(patch("/api/users/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code")
                            .value(ErrorCode.VALIDATION_ERROR.name()));

            verify(currentUserProvider, never()).getUserId();
            verify(userService, never()).updatePassword(any(), any());
        }
    }

    @Nested
    class DeleteAccountTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldDeleteAccount_whenValidRequest() throws Exception {

            Long userId = 1L;

            UserDeleteAccountRequest request =
                    new UserDeleteAccountRequest(UserTestDataProvider.PASSWORD);

            when(currentUserProvider.getUserId()).thenReturn(userId);

            mockMvc.perform(delete("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            verify(currentUserProvider).getUserId();
            verify(userService).deleteAccount(eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenCurrentPasswordIsMissing() throws Exception {

            UserDeleteAccountRequest request =
                    new UserDeleteAccountRequest("");

            mockMvc.perform(delete("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(currentUserProvider, never()).getUserId();
            verify(userService, never()).updatePassword(any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenPasswordIsIncorrect() throws Exception {

            Long userId = 1L;

            UserDeleteAccountRequest request =
                    new UserDeleteAccountRequest("WrongPassword");

            when(currentUserProvider.getUserId()).thenReturn(userId);

            doThrow(new InvalidPasswordException())
                    .when(userService).deleteAccount(eq(userId), any());

            mockMvc.perform(delete("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code")
                            .value(ErrorCode.INVALID_PASSWORD.name()));

            verify(currentUserProvider).getUserId();
            verify(userService).deleteAccount(eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn409_whenUserIsWorkspaceOwner() throws Exception {

            Long userId = 1L;

            UserDeleteAccountRequest request =
                    new UserDeleteAccountRequest(UserTestDataProvider.PASSWORD);

            when(currentUserProvider.getUserId()).thenReturn(userId);

            doThrow(new WorkspaceOwnerExistsException())
                    .when(userService).deleteAccount(eq(userId), any());

            mockMvc.perform(delete("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code")
                            .value(ErrorCode.WORKSPACE_OWNER_RESTRICTION.name()));

            verify(currentUserProvider).getUserId();
            verify(userService).deleteAccount(eq(userId), any());
        }
    }
}
