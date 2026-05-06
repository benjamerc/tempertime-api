package com.tempertime.tempertime_api.workspaces.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tempertime.tempertime_api.common.color.InvalidColorFormatException;
import com.tempertime.tempertime_api.common.error.ApiErrorBuilder;
import com.tempertime.tempertime_api.common.error.GlobalExceptionHandler;
import com.tempertime.tempertime_api.common.error.model.ErrorCode;
import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.security.core.CurrentUserProvider;
import com.tempertime.tempertime_api.security.jwt.AccessTokenService;
import com.tempertime.tempertime_api.security.jwt.JwtAuthenticationFilter;
import com.tempertime.tempertime_api.users.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.workspaces.WorkspaceTestDataProvider;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceInviteCode;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceUser;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceCreateRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceJoinRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceUpdateRequest;
import com.tempertime.tempertime_api.workspaces.dto.response.*;
import com.tempertime.tempertime_api.workspaces.exception.*;
import com.tempertime.tempertime_api.workspaces.service.core.WorkspaceService;
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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkspaceController.class)
@Import({GlobalExceptionHandler.class, ApiErrorBuilder.class})
@AutoConfigureMockMvc(addFilters = false)
public class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WorkspaceService workspaceService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @Nested
    class CreateWorkspaceTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldCreateWorkspace_whenValidRequest() throws Exception {

            Long userId = 1L;

            WorkspaceCreateRequest request =
                    new WorkspaceCreateRequest(WorkspaceTestDataProvider.NAME, WorkspaceTestDataProvider.COLOR);

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            WorkspaceCreateResponse response = WorkspaceTestDataProvider.workspaceCreateResponse(workspace);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(workspaceService.createWorkspace(any(), eq(userId))).thenReturn(response);

            mockMvc.perform(post("/api/workspaces")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(response.id()))
                    .andExpect(jsonPath("$.name").value(response.name()))
                    .andExpect(jsonPath("$.inviteCode").value(response.inviteCode()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).createWorkspace(any(), eq(userId));
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldCreateWorkspace_whenNoColorProvided() throws Exception {

            Long userId = 1L;

            WorkspaceCreateRequest request =
                    new WorkspaceCreateRequest(WorkspaceTestDataProvider.NAME, null);

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            WorkspaceCreateResponse response = WorkspaceTestDataProvider.workspaceCreateResponse(workspace);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(workspaceService.createWorkspace(any(), eq(userId))).thenReturn(response);

            mockMvc.perform(post("/api/workspaces")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(response.id()))
                    .andExpect(jsonPath("$.name").value(response.name()))
                    .andExpect(jsonPath("$.inviteCode").value(response.inviteCode()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).createWorkspace(any(), eq(userId));
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenNameIsMissing() throws Exception {

            WorkspaceCreateRequest request =
                    new WorkspaceCreateRequest(null, WorkspaceTestDataProvider.COLOR);

            mockMvc.perform(post("/api/workspaces")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(currentUserProvider, never()).getUserId();
            verify(workspaceService, never()).createWorkspace(any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenNameIsTooShort() throws Exception {

            WorkspaceCreateRequest request =
                    new WorkspaceCreateRequest("A", WorkspaceTestDataProvider.COLOR);

            mockMvc.perform(post("/api/workspaces")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(currentUserProvider, never()).getUserId();
            verify(workspaceService, never()).createWorkspace(any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenColorFormatIsInvalid() throws Exception {

            Long userId = 1L;

            WorkspaceCreateRequest request =
                    new WorkspaceCreateRequest(WorkspaceTestDataProvider.NAME, "blue");

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new InvalidColorFormatException())
                    .when(workspaceService).createWorkspace(any(), eq(userId));

            mockMvc.perform(post("/api/workspaces")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_COLOR_FORMAT.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).createWorkspace(any(), eq(userId));
        }
    }

    @Nested
    class GetUserWorkspacesTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnPagedWorkspaces_whenUserHasWorkspaces() throws Exception {

            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            WorkspaceListItemResponse listItemResponse =
                    WorkspaceTestDataProvider.workspaceListItemResponse(workspace, WorkspaceRole.OWNER);

            PageResponse<WorkspaceListItemResponse> pageResponse = new PageResponse<>(
                    List.of(listItemResponse), 0, 20, 1L, 1, true
            );

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(workspaceService.getUserWorkspaces(eq(userId), any(), any(), any()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/workspaces"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(workspace.getId()))
                    .andExpect(jsonPath("$.content[0].name").value(workspace.getName()))
                    .andExpect(jsonPath("$.content[0].color").value(workspace.getColor()))
                    .andExpect(jsonPath("$.content[0].userRole").value(WorkspaceRole.OWNER.name()))
                    .andExpect(jsonPath("$.content[0].archived").value(false))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.currentPage").value(0));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).getUserWorkspaces(eq(userId), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnEmptyPage_whenUserHasNoWorkspaces() throws Exception {

            Long userId = 1L;

            PageResponse<WorkspaceListItemResponse> pageResponse = new PageResponse<>(
                    List.of(), 0, 20, 0L, 0, true
            );

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(workspaceService.getUserWorkspaces(eq(userId), any(), any(), any()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/workspaces"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).getUserWorkspaces(eq(userId), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldFilterByRole_whenRoleParamIsProvided() throws Exception {

            Long userId = 1L;

            PageResponse<WorkspaceListItemResponse> pageResponse = new PageResponse<>(
                    List.of(), 0, 20, 0L, 0, true
            );

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(workspaceService.getUserWorkspaces(eq(userId), eq(WorkspaceRole.OWNER), any(), any()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/workspaces")
                            .param("role", "OWNER"))
                    .andExpect(status().isOk());

            verify(workspaceService).getUserWorkspaces(eq(userId), eq(WorkspaceRole.OWNER), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldFilterByArchivedStatus_whenArchivedParamIsProvided() throws Exception {

            Long userId = 1L;

            PageResponse<WorkspaceListItemResponse> pageResponse = new PageResponse<>(
                    List.of(), 0, 20, 0L, 0, true
            );

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(workspaceService.getUserWorkspaces(eq(userId), any(), eq(true), any()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/workspaces")
                            .param("archived", "true"))
                    .andExpect(status().isOk());

            verify(workspaceService).getUserWorkspaces(eq(userId), any(), eq(true), any());
        }
    }

    @Nested
    class GetWorkspaceByIdTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnWorkspaceDetail_whenUserIsInWorkspace() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            WorkspaceDetailResponse response =
                    WorkspaceTestDataProvider.workspaceDetailResponse(workspace, WorkspaceRole.OWNER);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(workspaceService.getWorkspaceById(workspaceId, userId)).thenReturn(response);

            mockMvc.perform(get("/api/workspaces/{workspaceId}", workspaceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(workspaceId))
                    .andExpect(jsonPath("$.name").value(response.name()))
                    .andExpect(jsonPath("$.color").value(response.color()))
                    .andExpect(jsonPath("$.userRole").value(WorkspaceRole.OWNER.name()))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.archived").value(false));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).getWorkspaceById(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(workspaceService).getWorkspaceById(workspaceId, userId);

            mockMvc.perform(get("/api/workspaces/{workspaceId}", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).getWorkspaceById(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(workspaceService).getWorkspaceById(workspaceId, userId);

            mockMvc.perform(get("/api/workspaces/{workspaceId}", workspaceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).getWorkspaceById(workspaceId, userId);
        }
    }

    @Nested
    class UpdateWorkspaceTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldUpdateWorkspace_whenValidRequest() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            WorkspaceUpdateRequest request =
                    new WorkspaceUpdateRequest(WorkspaceTestDataProvider.NAME, WorkspaceTestDataProvider.COLOR);

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            WorkspaceUpdateResponse response = WorkspaceTestDataProvider.workspaceUpdateResponse(workspace);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(workspaceService.updateWorkspace(eq(workspaceId), eq(userId), any())).thenReturn(response);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}", workspaceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(workspaceId))
                    .andExpect(jsonPath("$.name").value(response.name()))
                    .andExpect(jsonPath("$.color").value(response.color()))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.archived").value(false));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).updateWorkspace(eq(workspaceId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenNameIsTooShort() throws Exception {

            WorkspaceUpdateRequest request =
                    new WorkspaceUpdateRequest("A", WorkspaceTestDataProvider.COLOR);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(currentUserProvider, never()).getUserId();
            verify(workspaceService, never()).updateWorkspace(any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenColorFormatIsInvalid() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            WorkspaceUpdateRequest request =
                    new WorkspaceUpdateRequest(WorkspaceTestDataProvider.NAME, "blue");

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new InvalidColorFormatException())
                    .when(workspaceService).updateWorkspace(eq(workspaceId), eq(userId), any());

            mockMvc.perform(patch("/api/workspaces/{workspaceId}", workspaceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_COLOR_FORMAT.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).updateWorkspace(eq(workspaceId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;

            WorkspaceUpdateRequest request =
                    new WorkspaceUpdateRequest(WorkspaceTestDataProvider.NAME, WorkspaceTestDataProvider.COLOR);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(workspaceService).updateWorkspace(eq(workspaceId), eq(userId), any());

            mockMvc.perform(patch("/api/workspaces/{workspaceId}", workspaceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).updateWorkspace(eq(workspaceId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotOwner() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;

            WorkspaceUpdateRequest request =
                    new WorkspaceUpdateRequest(WorkspaceTestDataProvider.NAME, WorkspaceTestDataProvider.COLOR);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceRoleDeniedException())
                    .when(workspaceService).updateWorkspace(eq(workspaceId), eq(userId), any());

            mockMvc.perform(patch("/api/workspaces/{workspaceId}", workspaceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ROLE_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).updateWorkspace(eq(workspaceId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;

            WorkspaceUpdateRequest request =
                    new WorkspaceUpdateRequest(WorkspaceTestDataProvider.NAME, WorkspaceTestDataProvider.COLOR);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(workspaceService).updateWorkspace(eq(workspaceId), eq(userId), any());

            mockMvc.perform(patch("/api/workspaces/{workspaceId}", workspaceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).updateWorkspace(eq(workspaceId), eq(userId), any());
        }
    }

    @Nested
    class ArchiveWorkspaceTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldArchiveWorkspace_whenUserIsOwner() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/archive", workspaceId))
                    .andExpect(status().isNoContent());

            verify(currentUserProvider).getUserId();
            verify(workspaceService).archiveWorkspace(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(workspaceService).archiveWorkspace(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/archive", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).archiveWorkspace(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotOwner() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceRoleDeniedException())
                    .when(workspaceService).archiveWorkspace(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/archive", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ROLE_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).archiveWorkspace(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(workspaceService).archiveWorkspace(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/archive", workspaceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).archiveWorkspace(workspaceId, userId);
        }
    }

    @Nested
    class UnarchiveWorkspaceTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldUnarchiveWorkspace_whenUserIsOwner() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/unarchive", workspaceId))
                    .andExpect(status().isNoContent());

            verify(currentUserProvider).getUserId();
            verify(workspaceService).unarchiveWorkspace(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(workspaceService).unarchiveWorkspace(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/unarchive", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).unarchiveWorkspace(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotOwner() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceRoleDeniedException())
                    .when(workspaceService).unarchiveWorkspace(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/unarchive", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ROLE_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).unarchiveWorkspace(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(workspaceService).unarchiveWorkspace(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/unarchive", workspaceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).unarchiveWorkspace(workspaceId, userId);
        }
    }

    @Nested
    class DeleteWorkspaceTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldDeleteWorkspace_whenWorkspaceIsArchived() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}", workspaceId))
                    .andExpect(status().isNoContent());

            verify(currentUserProvider).getUserId();
            verify(workspaceService).deleteWorkspace(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn409_whenWorkspaceIsNotArchived() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotArchivedException())
                    .when(workspaceService).deleteWorkspace(workspaceId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}", workspaceId))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_ARCHIVED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).deleteWorkspace(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(workspaceService).deleteWorkspace(workspaceId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).deleteWorkspace(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotOwner() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceRoleDeniedException())
                    .when(workspaceService).deleteWorkspace(workspaceId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ROLE_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).deleteWorkspace(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(workspaceService).deleteWorkspace(workspaceId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}", workspaceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).deleteWorkspace(workspaceId, userId);
        }
    }

    @Nested
    class GetInviteCodeTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnInviteCode_whenUserIsOwner() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            WorkspaceInviteCode inviteCode = WorkspaceTestDataProvider.inviteCode(workspace);
            WorkspaceInviteCodeResponse response =
                    WorkspaceTestDataProvider.workspaceInviteCodeResponse(inviteCode);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(workspaceService.getInviteCode(workspaceId, userId)).thenReturn(response);

            mockMvc.perform(get("/api/workspaces/{workspaceId}/invite-code", workspaceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inviteCode").value(response.inviteCode()))
                    .andExpect(jsonPath("$.inviteEnabled").value(response.inviteEnabled()))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty());

            verify(currentUserProvider).getUserId();
            verify(workspaceService).getInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(workspaceService).getInviteCode(workspaceId, userId);

            mockMvc.perform(get("/api/workspaces/{workspaceId}/invite-code", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).getInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotOwner() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceRoleDeniedException())
                    .when(workspaceService).getInviteCode(workspaceId, userId);

            mockMvc.perform(get("/api/workspaces/{workspaceId}/invite-code", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ROLE_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).getInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(workspaceService).getInviteCode(workspaceId, userId);

            mockMvc.perform(get("/api/workspaces/{workspaceId}/invite-code", workspaceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).getInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenInviteCodeDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceInviteCodeNotFoundException())
                    .when(workspaceService).getInviteCode(workspaceId, userId);

            mockMvc.perform(get("/api/workspaces/{workspaceId}/invite-code", workspaceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_INVITE_CODE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).getInviteCode(workspaceId, userId);
        }
    }

    @Nested
    class ActivateInviteCodeTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldActivateInviteCode_whenUserIsOwner() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            WorkspaceInviteCodeStatusResponse response = new WorkspaceInviteCodeStatusResponse(true);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(workspaceService.activateInviteCode(workspaceId, userId)).thenReturn(response);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/invite-code/enable", workspaceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inviteEnabled").value(true));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).activateInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(workspaceService).activateInviteCode(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/invite-code/enable", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).activateInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotOwner() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceRoleDeniedException())
                    .when(workspaceService).activateInviteCode(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/invite-code/enable", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ROLE_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).activateInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(workspaceService).activateInviteCode(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/invite-code/enable", workspaceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).activateInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenInviteCodeDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceInviteCodeNotFoundException())
                    .when(workspaceService).activateInviteCode(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/invite-code/enable", workspaceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_INVITE_CODE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).activateInviteCode(workspaceId, userId);
        }
    }

    @Nested
    class DeactivateInviteCodeTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldDeactivateInviteCode_whenUserIsOwner() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            WorkspaceInviteCodeStatusResponse response = new WorkspaceInviteCodeStatusResponse(false);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(workspaceService.deactivateInviteCode(workspaceId, userId)).thenReturn(response);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/invite-code/disable", workspaceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inviteEnabled").value(false));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).deactivateInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(workspaceService).deactivateInviteCode(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/invite-code/disable", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).deactivateInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotOwner() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceRoleDeniedException())
                    .when(workspaceService).deactivateInviteCode(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/invite-code/disable", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ROLE_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).deactivateInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(workspaceService).deactivateInviteCode(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/invite-code/disable", workspaceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).deactivateInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenInviteCodeDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceInviteCodeNotFoundException())
                    .when(workspaceService).deactivateInviteCode(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/invite-code/disable", workspaceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_INVITE_CODE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).deactivateInviteCode(workspaceId, userId);
        }
    }

    @Nested
    class RegenerateInviteCodeTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldRegenerateInviteCode_whenUserIsOwner() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            WorkspaceInviteCode inviteCode = WorkspaceTestDataProvider.inviteCode(workspace);
            WorkspaceInviteCodeResponse response =
                    WorkspaceTestDataProvider.workspaceInviteCodeResponse(inviteCode);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(workspaceService.regenerateInviteCode(workspaceId, userId)).thenReturn(response);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/invite-code/regenerate", workspaceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inviteCode").value(response.inviteCode()))
                    .andExpect(jsonPath("$.inviteEnabled").value(response.inviteEnabled()))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty());

            verify(currentUserProvider).getUserId();
            verify(workspaceService).regenerateInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(workspaceService).regenerateInviteCode(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/invite-code/regenerate", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).regenerateInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotOwner() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceRoleDeniedException())
                    .when(workspaceService).regenerateInviteCode(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/invite-code/regenerate", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ROLE_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).regenerateInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(workspaceService).regenerateInviteCode(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/invite-code/regenerate", workspaceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).regenerateInviteCode(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenInviteCodeDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceInviteCodeNotFoundException())
                    .when(workspaceService).regenerateInviteCode(workspaceId, userId);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/invite-code/regenerate", workspaceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_INVITE_CODE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).regenerateInviteCode(workspaceId, userId);
        }
    }

    @Nested
    class JoinWorkspaceTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldJoinWorkspace_whenValidInviteCode() throws Exception {

            Long userId = 1L;

            WorkspaceJoinRequest request =
                    new WorkspaceJoinRequest(WorkspaceTestDataProvider.INVITE_CODE);

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            User user = UserTestDataProvider.user(userId);
            WorkspaceUser workspaceUser = WorkspaceTestDataProvider.memberWorkspaceUser(workspace, user);
            WorkspaceJoinResponse response = WorkspaceTestDataProvider.workspaceJoinResponse(workspaceUser);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(workspaceService.joinWorkspace(request.inviteCode(), userId)).thenReturn(response);

            mockMvc.perform(post("/api/workspaces/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.workspaceId").value(response.workspaceId()))
                    .andExpect(jsonPath("$.userId").value(response.userId()))
                    .andExpect(jsonPath("$.role").value(WorkspaceRole.MEMBER.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).joinWorkspace(request.inviteCode(), userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenInviteCodeIsMissing() throws Exception {

            WorkspaceJoinRequest request = new WorkspaceJoinRequest(null);

            mockMvc.perform(post("/api/workspaces/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(currentUserProvider, never()).getUserId();
            verify(workspaceService, never()).joinWorkspace(any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenInviteCodeIsWrongLength() throws Exception {

            WorkspaceJoinRequest request = new WorkspaceJoinRequest("SHORT");

            mockMvc.perform(post("/api/workspaces/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(currentUserProvider, never()).getUserId();
            verify(workspaceService, never()).joinWorkspace(any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenInviteCodeIsInvalid() throws Exception {

            Long userId = 1L;

            WorkspaceJoinRequest request =
                    new WorkspaceJoinRequest(WorkspaceTestDataProvider.INVITE_CODE);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new InvalidWorkspaceInviteCodeException())
                    .when(workspaceService).joinWorkspace(request.inviteCode(), userId);

            mockMvc.perform(post("/api/workspaces/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_WORKSPACE_INVITE_CODE.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).joinWorkspace(request.inviteCode(), userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn409_whenInviteCodeIsDisabled() throws Exception {

            Long userId = 1L;

            WorkspaceJoinRequest request =
                    new WorkspaceJoinRequest(WorkspaceTestDataProvider.INVITE_CODE);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceInviteCodeDisabledException())
                    .when(workspaceService).joinWorkspace(request.inviteCode(), userId);

            mockMvc.perform(post("/api/workspaces/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_INVITE_CODE_DISABLED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).joinWorkspace(request.inviteCode(), userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn409_whenUserAlreadyInWorkspace() throws Exception {

            Long userId = 1L;

            WorkspaceJoinRequest request =
                    new WorkspaceJoinRequest(WorkspaceTestDataProvider.INVITE_CODE);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new UserAlreadyInWorkspaceException())
                    .when(workspaceService).joinWorkspace(request.inviteCode(), userId);

            mockMvc.perform(post("/api/workspaces/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCode.USER_ALREADY_IN_WORKSPACE.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).joinWorkspace(request.inviteCode(), userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn409_whenWorkspaceCapacityExceeded() throws Exception {

            Long userId = 1L;

            WorkspaceJoinRequest request =
                    new WorkspaceJoinRequest(WorkspaceTestDataProvider.INVITE_CODE);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceCapacityExceededException())
                    .when(workspaceService).joinWorkspace(request.inviteCode(), userId);

            mockMvc.perform(post("/api/workspaces/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_CAPACITY_EXCEEDED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).joinWorkspace(request.inviteCode(), userId);
        }
    }
    @Nested
    class GetWorkspaceUsersTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnPagedUsers_whenUserIsInWorkspace() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            User user = UserTestDataProvider.user(userId);
            WorkspaceUserResponse userResponse = new WorkspaceUserResponse(
                    userId, user.getFirstName(), user.getLastName(), WorkspaceRole.OWNER
            );

            PageResponse<WorkspaceUserResponse> pageResponse = new PageResponse<>(
                    List.of(userResponse), 0, 20, 1L, 1, true
            );

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(workspaceService.getWorkspaceUsers(eq(workspaceId), eq(userId), any()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/workspaces/{workspaceId}/users", workspaceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(userId))
                    .andExpect(jsonPath("$.content[0].firstName").value(user.getFirstName()))
                    .andExpect(jsonPath("$.content[0].lastName").value(user.getLastName()))
                    .andExpect(jsonPath("$.content[0].role").value(WorkspaceRole.OWNER.name()))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.currentPage").value(0));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).getWorkspaceUsers(eq(workspaceId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnEmptyPage_whenWorkspaceHasNoUsers() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            PageResponse<WorkspaceUserResponse> pageResponse = new PageResponse<>(
                    List.of(), 0, 20, 0L, 0, true
            );

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(workspaceService.getWorkspaceUsers(eq(workspaceId), eq(userId), any()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/workspaces/{workspaceId}/users", workspaceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).getWorkspaceUsers(eq(workspaceId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(workspaceService).getWorkspaceUsers(eq(workspaceId), eq(userId), any());

            mockMvc.perform(get("/api/workspaces/{workspaceId}/users", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).getWorkspaceUsers(eq(workspaceId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(workspaceService).getWorkspaceUsers(eq(workspaceId), eq(userId), any());

            mockMvc.perform(get("/api/workspaces/{workspaceId}/users", workspaceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).getWorkspaceUsers(eq(workspaceId), eq(userId), any());
        }
    }

    @Nested
    class RemoveWorkspaceUserTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldRemoveUser_whenUserIsOwnerAndTargetIsInWorkspace() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long targetUserId = 2L;

            when(currentUserProvider.getUserId()).thenReturn(userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/users/{targetUserId}",
                            workspaceId, targetUserId))
                    .andExpect(status().isNoContent());

            verify(currentUserProvider).getUserId();
            verify(workspaceService).removeWorkspaceUser(workspaceId, targetUserId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;
            Long targetUserId = 2L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(workspaceService).removeWorkspaceUser(workspaceId, targetUserId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/users/{targetUserId}",
                            workspaceId, targetUserId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).removeWorkspaceUser(workspaceId, targetUserId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotOwner() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;
            Long targetUserId = 3L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceRoleDeniedException())
                    .when(workspaceService).removeWorkspaceUser(workspaceId, targetUserId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/users/{targetUserId}",
                            workspaceId, targetUserId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ROLE_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).removeWorkspaceUser(workspaceId, targetUserId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenTargetIsOwner() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long targetUserId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceOperationNotAllowedException())
                    .when(workspaceService).removeWorkspaceUser(workspaceId, targetUserId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/users/{targetUserId}",
                            workspaceId, targetUserId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_OPERATION_NOT_ALLOWED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).removeWorkspaceUser(workspaceId, targetUserId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;
            Long targetUserId = 2L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(workspaceService).removeWorkspaceUser(workspaceId, targetUserId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/users/{targetUserId}",
                            workspaceId, targetUserId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).removeWorkspaceUser(workspaceId, targetUserId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenTargetUserIsNotInWorkspace() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long targetUserId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceUserNotFoundException())
                    .when(workspaceService).removeWorkspaceUser(workspaceId, targetUserId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/users/{targetUserId}",
                            workspaceId, targetUserId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_USER_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).removeWorkspaceUser(workspaceId, targetUserId, userId);
        }
    }

    @Nested
    class LeaveWorkspaceTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldLeaveWorkspace_whenUserIsInWorkspace() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/users/me", workspaceId))
                    .andExpect(status().isNoContent());

            verify(currentUserProvider).getUserId();
            verify(workspaceService).leaveWorkspace(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(workspaceService).leaveWorkspace(workspaceId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/users/me", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).leaveWorkspace(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsOwner() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceOperationNotAllowedException())
                    .when(workspaceService).leaveWorkspace(workspaceId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/users/me", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_OPERATION_NOT_ALLOWED.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).leaveWorkspace(workspaceId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(workspaceService).leaveWorkspace(workspaceId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/users/me", workspaceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(workspaceService).leaveWorkspace(workspaceId, userId);
        }
    }
}
