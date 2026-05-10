package com.tempertime.tempertime_api.events.controller;

import com.tempertime.tempertime_api.common.error.ApiErrorBuilder;
import com.tempertime.tempertime_api.common.error.GlobalExceptionHandler;
import com.tempertime.tempertime_api.common.error.model.ErrorCode;
import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.events.EventTestDataProvider;
import com.tempertime.tempertime_api.events.domain.Event;
import com.tempertime.tempertime_api.events.domain.EventPeriod;
import com.tempertime.tempertime_api.events.dto.response.EventAssignedUserResponse;
import com.tempertime.tempertime_api.events.dto.response.EventAssignUserResponse;
import com.tempertime.tempertime_api.events.dto.response.EventCreateResponse;
import com.tempertime.tempertime_api.events.dto.response.EventListItemResponse;
import com.tempertime.tempertime_api.events.dto.response.EventResponse;
import com.tempertime.tempertime_api.events.exception.EventAccessDeniedException;
import com.tempertime.tempertime_api.events.exception.EventDateLimitExceededException;
import com.tempertime.tempertime_api.events.exception.EventNotFoundException;
import com.tempertime.tempertime_api.events.exception.EventNotAssignableException;
import com.tempertime.tempertime_api.events.exception.TimeZoneMissingException;
import com.tempertime.tempertime_api.events.exception.UserNotAssignedToEventException;
import com.tempertime.tempertime_api.events.service.core.EventService;
import com.tempertime.tempertime_api.common.color.InvalidColorFormatException;
import com.tempertime.tempertime_api.security.core.CurrentUserProvider;
import com.tempertime.tempertime_api.security.jwt.AccessTokenService;
import com.tempertime.tempertime_api.security.jwt.JwtAuthenticationFilter;
import com.tempertime.tempertime_api.events.support.EventTestDateFactory;
import com.tempertime.tempertime_api.users.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import com.tempertime.tempertime_api.workspaces.WorkspaceTestDataProvider;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceAccessDeniedException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceNotFoundException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceRoleDeniedException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@Import({GlobalExceptionHandler.class, ApiErrorBuilder.class})
@AutoConfigureMockMvc(addFilters = false)
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @Nested
    class CreateEventTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldCreateEvent_whenValidRequest() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(1L, workspace);
            EventCreateResponse response = EventTestDataProvider.eventCreateResponse(event);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(eventService.createEvent(eq(workspaceId), eq(userId), any())).thenReturn(response);

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events", workspaceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "title": "Test Event",
                                  "eventDate": "%s",
                                  "description": "Test Description",
                                  "scope": "GLOBAL",
                                  "color": "#A3B4C5"
                                }
                                """.formatted(EventTestDateFactory.futureDate())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(response.id()))
                    .andExpect(jsonPath("$.title").value(response.title()))
                    .andExpect(jsonPath("$.eventDate").isNotEmpty())
                    .andExpect(jsonPath("$.createdAt").isNotEmpty());

            verify(currentUserProvider).getUserId();
            verify(eventService).createEvent(eq(workspaceId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenTitleIsMissing() throws Exception {

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "eventDate": "%s",
                                  "scope": "GLOBAL"
                                }
                                """.formatted(EventTestDateFactory.futureDate())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(currentUserProvider, never()).getUserId();
            verify(eventService, never()).createEvent(any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenTitleIsTooShort() throws Exception {

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "title": "AB",
                                  "eventDate": "%s",
                                  "scope": "GLOBAL"
                                }
                                """.formatted(EventTestDateFactory.futureDate())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(currentUserProvider, never()).getUserId();
            verify(eventService, never()).createEvent(any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenScopeIsMissing() throws Exception {

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "title": "Test Event",
                                  "eventDate": "%s"
                                }
                                """.formatted(EventTestDateFactory.futureDate())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(currentUserProvider, never()).getUserId();
            verify(eventService, never()).createEvent(any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenColorFormatIsInvalid() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new InvalidColorFormatException())
                    .when(eventService).createEvent(eq(workspaceId), eq(userId), any());

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events", workspaceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "title": "Test Event",
                                  "eventDate": "%s",
                                  "scope": "GLOBAL",
                                  "color": "blue"
                                }
                                """.formatted(EventTestDateFactory.futureDate())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_COLOR_FORMAT.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).createEvent(eq(workspaceId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenEventDateLimitExceeded() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new EventDateLimitExceededException())
                    .when(eventService).createEvent(eq(workspaceId), eq(userId), any());

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events", workspaceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "title": "Test Event",
                                  "eventDate": "%s",
                                  "scope": "GLOBAL"
                                }
                                """.formatted(EventTestDateFactory.futureDate())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.EVENT_DATE_LIMIT_EXCEEDED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).createEvent(eq(workspaceId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(eventService).createEvent(eq(workspaceId), eq(userId), any());

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events", workspaceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "title": "Test Event",
                                  "eventDate": "%s",
                                  "scope": "GLOBAL"
                                }
                                """.formatted(EventTestDateFactory.futureDate())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).createEvent(eq(workspaceId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotOwner() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceRoleDeniedException())
                    .when(eventService).createEvent(eq(workspaceId), eq(userId), any());

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events", workspaceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "title": "Test Event",
                                  "eventDate": "%s",
                                  "scope": "GLOBAL"
                                }
                                """.formatted(EventTestDateFactory.futureDate())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ROLE_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).createEvent(eq(workspaceId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(eventService).createEvent(eq(workspaceId), eq(userId), any());

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events", workspaceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "title": "Test Event",
                                  "eventDate": "%s",
                                  "scope": "GLOBAL"
                                }
                                """.formatted(EventTestDateFactory.futureDate())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).createEvent(eq(workspaceId), eq(userId), any());
        }
    }

    @Nested
    class GetEventsTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnPagedEvents_whenValidRequest() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(1L, workspace);
            EventListItemResponse listItemResponse = EventTestDataProvider.eventListItemResponse(event);

            PageResponse<EventListItemResponse> pageResponse = new PageResponse<>(
                    List.of(listItemResponse), 0, 20, 1L, 1, true
            );

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(eventService.getEvents(eq(workspaceId), eq(userId), any(), any(), any(), any()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/workspaces/{workspaceId}/events", workspaceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(event.getId()))
                    .andExpect(jsonPath("$.content[0].title").value(event.getTitle()))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(currentUserProvider).getUserId();
            verify(eventService).getEvents(eq(workspaceId), eq(userId), any(), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldFilterByPeriod_whenPeriodParamIsProvided() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            PageResponse<EventListItemResponse> pageResponse = new PageResponse<>(
                    List.of(), 0, 20, 0L, 0, true
            );

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(eventService.getEvents(eq(workspaceId), eq(userId), eq(EventPeriod.DAY), any(), any(), any()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/workspaces/{workspaceId}/events", workspaceId)
                            .param("period", "DAY")
                            .param("timeZone", "America/Argentina/Buenos_Aires"))
                    .andExpect(status().isOk());

            verify(eventService).getEvents(eq(workspaceId), eq(userId), eq(EventPeriod.DAY), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenTimeZoneIsMissing() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new TimeZoneMissingException())
                    .when(eventService).getEvents(eq(workspaceId), eq(userId), any(), any(), any(), any());

            mockMvc.perform(get("/api/workspaces/{workspaceId}/events", workspaceId)
                            .param("period", "DAY"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.TIME_ZONE_MISSING.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).getEvents(eq(workspaceId), eq(userId), any(), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(eventService).getEvents(eq(workspaceId), eq(userId), any(), any(), any(), any());

            mockMvc.perform(get("/api/workspaces/{workspaceId}/events", workspaceId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).getEvents(eq(workspaceId), eq(userId), any(), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(eventService).getEvents(eq(workspaceId), eq(userId), any(), any(), any(), any());

            mockMvc.perform(get("/api/workspaces/{workspaceId}/events", workspaceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).getEvents(eq(workspaceId), eq(userId), any(), any(), any(), any());
        }
    }

    @Nested
    class GetEventTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnEvent_whenUserHasAccess() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);
            EventResponse response = EventTestDataProvider.eventResponse(event);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(eventService.getEvent(workspaceId, eventId, userId)).thenReturn(response);

            mockMvc.perform(get("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(eventId))
                    .andExpect(jsonPath("$.title").value(response.title()))
                    .andExpect(jsonPath("$.eventDate").isNotEmpty())
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.description").value(response.description()))
                    .andExpect(jsonPath("$.scope").value(response.scope()))
                    .andExpect(jsonPath("$.color").value(response.color()))
                    .andExpect(jsonPath("$.hasActiveUsers").value(response.hasActiveUsers()));

            verify(currentUserProvider).getUserId();
            verify(eventService).getEvent(workspaceId, eventId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(eventService).getEvent(workspaceId, eventId, userId);

            mockMvc.perform(get("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).getEvent(workspaceId, eventId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotAssignedToEvent() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new EventAccessDeniedException())
                    .when(eventService).getEvent(workspaceId, eventId, userId);

            mockMvc.perform(get("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.EVENT_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).getEvent(workspaceId, eventId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(eventService).getEvent(workspaceId, eventId, userId);

            mockMvc.perform(get("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).getEvent(workspaceId, eventId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenEventDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new EventNotFoundException())
                    .when(eventService).getEvent(workspaceId, eventId, userId);

            mockMvc.perform(get("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.EVENT_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).getEvent(workspaceId, eventId, userId);
        }
    }

    @Nested
    class UpdateEventTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldUpdateEvent_whenValidRequest() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);
            EventResponse response = EventTestDataProvider.eventResponse(event);

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(eventService.updateEvent(eq(workspaceId), eq(eventId), eq(userId), any()))
                    .thenReturn(response);

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "title": "Updated Title",
                                  "eventDate": "%s",
                                  "color": "#B2C3D4"
                                }
                                """.formatted(EventTestDateFactory.futureDate())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(eventId))
                    .andExpect(jsonPath("$.title").value(response.title()))
                    .andExpect(jsonPath("$.eventDate").isNotEmpty())
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.scope").value(response.scope()))
                    .andExpect(jsonPath("$.hasActiveUsers").value(response.hasActiveUsers()));

            verify(currentUserProvider).getUserId();
            verify(eventService).updateEvent(eq(workspaceId), eq(eventId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenTitleIsTooShort() throws Exception {

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/events/{eventId}", 1L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "title": "AB"
                                }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(currentUserProvider, never()).getUserId();
            verify(eventService, never()).updateEvent(any(), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenColorFormatIsInvalid() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new InvalidColorFormatException())
                    .when(eventService).updateEvent(eq(workspaceId), eq(eventId), eq(userId), any());

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "color": "blue"
                                }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_COLOR_FORMAT.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).updateEvent(eq(workspaceId), eq(eventId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenEventDateLimitExceeded() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new EventDateLimitExceededException())
                    .when(eventService).updateEvent(eq(workspaceId), eq(eventId), eq(userId), any());

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "eventDate": "%s"
                                }
                                """.formatted(EventTestDateFactory.futureDate())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.EVENT_DATE_LIMIT_EXCEEDED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).updateEvent(eq(workspaceId), eq(eventId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(eventService).updateEvent(eq(workspaceId), eq(eventId), eq(userId), any());

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "title": "Updated Title"
                                }
                                """))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).updateEvent(eq(workspaceId), eq(eventId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotOwner() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceRoleDeniedException())
                    .when(eventService).updateEvent(eq(workspaceId), eq(eventId), eq(userId), any());

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "title": "Updated Title"
                                }
                                """))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ROLE_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).updateEvent(eq(workspaceId), eq(eventId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(eventService).updateEvent(eq(workspaceId), eq(eventId), eq(userId), any());

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "title": "Updated Title"
                                }
                                """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).updateEvent(eq(workspaceId), eq(eventId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenEventDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new EventNotFoundException())
                    .when(eventService).updateEvent(eq(workspaceId), eq(eventId), eq(userId), any());

            mockMvc.perform(patch("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "title": "Updated Title"
                                }
                                """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.EVENT_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).updateEvent(eq(workspaceId), eq(eventId), eq(userId), any());
        }
    }

    @Nested
    class DeleteEventTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldDeleteEvent_whenUserIsOwner() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId))
                    .andExpect(status().isNoContent());

            verify(currentUserProvider).getUserId();
            verify(eventService).deleteEvent(workspaceId, eventId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(eventService).deleteEvent(workspaceId, eventId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).deleteEvent(workspaceId, eventId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotOwner() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceRoleDeniedException())
                    .when(eventService).deleteEvent(workspaceId, eventId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ROLE_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).deleteEvent(workspaceId, eventId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(eventService).deleteEvent(workspaceId, eventId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).deleteEvent(workspaceId, eventId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenEventDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new EventNotFoundException())
                    .when(eventService).deleteEvent(workspaceId, eventId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/events/{eventId}",
                            workspaceId, eventId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.EVENT_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).deleteEvent(workspaceId, eventId, userId);
        }
    }

    @Nested
    class AssignUsersToEventTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldAssignUsers_whenValidRequest() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            EventAssignUserResponse response = new EventAssignUserResponse(eventId, List.of(2L, 3L));

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(userRepository.countByIdIn(List.of(2L, 3L))).thenReturn(2L);
            when(eventService.assignUsersToEvent(eq(workspaceId), eq(eventId), eq(userId), any()))
                    .thenReturn(response);

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events/{eventId}/users",
                            workspaceId, eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "userIds": [2, 3]
                            }
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.eventId").value(eventId))
                    .andExpect(jsonPath("$.userIds").isArray())
                    .andExpect(jsonPath("$.userIds[0]").value(2L))
                    .andExpect(jsonPath("$.userIds[1]").value(3L));

            verify(currentUserProvider).getUserId();
            verify(eventService).assignUsersToEvent(eq(workspaceId), eq(eventId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenUserIdsIsEmpty() throws Exception {

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events/{eventId}/users", 1L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "userIds": []
                                }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(currentUserProvider, never()).getUserId();
            verify(eventService, never()).assignUsersToEvent(any(), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenUserIdsIsMissing() throws Exception {

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events/{eventId}/users", 1L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {}
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

            verify(currentUserProvider, never()).getUserId();
            verify(eventService, never()).assignUsersToEvent(any(), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(userRepository.countByIdIn(List.of(2L))).thenReturn(1L);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(eventService).assignUsersToEvent(eq(workspaceId), eq(eventId), eq(userId), any());

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events/{eventId}/users",
                            workspaceId, eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "userIds": [2]
                            }
                            """))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).assignUsersToEvent(eq(workspaceId), eq(eventId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotOwner() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(userRepository.countByIdIn(List.of(3L))).thenReturn(1L);
            doThrow(new WorkspaceRoleDeniedException())
                    .when(eventService).assignUsersToEvent(eq(workspaceId), eq(eventId), eq(userId), any());

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events/{eventId}/users",
                            workspaceId, eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "userIds": [3]
                            }
                            """))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ROLE_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).assignUsersToEvent(eq(workspaceId), eq(eventId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(userRepository.countByIdIn(List.of(2L))).thenReturn(1L);
            doThrow(new WorkspaceNotFoundException())
                    .when(eventService).assignUsersToEvent(eq(workspaceId), eq(eventId), eq(userId), any());

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events/{eventId}/users",
                            workspaceId, eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "userIds": [2]
                            }
                            """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).assignUsersToEvent(eq(workspaceId), eq(eventId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenEventDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(userRepository.countByIdIn(List.of(2L))).thenReturn(1L);
            doThrow(new EventNotFoundException())
                    .when(eventService).assignUsersToEvent(eq(workspaceId), eq(eventId), eq(userId), any());

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events/{eventId}/users",
                            workspaceId, eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "userIds": [2]
                            }
                            """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.EVENT_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).assignUsersToEvent(eq(workspaceId), eq(eventId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn409_whenEventIsNotAssignable() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(userRepository.countByIdIn(List.of(2L))).thenReturn(1L);
            doThrow(new EventNotAssignableException())
                    .when(eventService).assignUsersToEvent(eq(workspaceId), eq(eventId), eq(userId), any());

            mockMvc.perform(post("/api/workspaces/{workspaceId}/events/{eventId}/users",
                            workspaceId, eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "userIds": [2]
                            }
                            """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCode.EVENT_NOT_ASSIGNABLE.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).assignUsersToEvent(eq(workspaceId), eq(eventId), eq(userId), any());
        }
    }

    @Nested
    class GetEventAssignedUsersTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnPagedAssignedUsers_whenUserHasAccess() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            User user = UserTestDataProvider.user(userId);
            EventAssignedUserResponse assignedUserResponse =
                    EventTestDataProvider.eventAssignedUserResponse(user);

            PageResponse<EventAssignedUserResponse> pageResponse = new PageResponse<>(
                    List.of(assignedUserResponse), 0, 20, 1L, 1, true
            );

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(eventService.getEventAssignedUsers(eq(workspaceId), eq(eventId), eq(userId), any()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/workspaces/{workspaceId}/events/{eventId}/users",
                            workspaceId, eventId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].userId").value(userId))
                    .andExpect(jsonPath("$.content[0].firstName").value(user.getFirstName()))
                    .andExpect(jsonPath("$.content[0].lastName").value(user.getLastName()))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(currentUserProvider).getUserId();
            verify(eventService).getEventAssignedUsers(eq(workspaceId), eq(eventId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(eventService).getEventAssignedUsers(eq(workspaceId), eq(eventId), eq(userId), any());

            mockMvc.perform(get("/api/workspaces/{workspaceId}/events/{eventId}/users",
                            workspaceId, eventId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).getEventAssignedUsers(eq(workspaceId), eq(eventId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotAssignedToEvent() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new EventAccessDeniedException())
                    .when(eventService).getEventAssignedUsers(eq(workspaceId), eq(eventId), eq(userId), any());

            mockMvc.perform(get("/api/workspaces/{workspaceId}/events/{eventId}/users",
                            workspaceId, eventId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.EVENT_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).getEventAssignedUsers(eq(workspaceId), eq(eventId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;
            Long eventId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(eventService).getEventAssignedUsers(eq(workspaceId), eq(eventId), eq(userId), any());

            mockMvc.perform(get("/api/workspaces/{workspaceId}/events/{eventId}/users",
                            workspaceId, eventId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).getEventAssignedUsers(eq(workspaceId), eq(eventId), eq(userId), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenEventDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new EventNotFoundException())
                    .when(eventService).getEventAssignedUsers(eq(workspaceId), eq(eventId), eq(userId), any());

            mockMvc.perform(get("/api/workspaces/{workspaceId}/events/{eventId}/users",
                            workspaceId, eventId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.EVENT_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).getEventAssignedUsers(eq(workspaceId), eq(eventId), eq(userId), any());
        }
    }

    @Nested
    class DeleteUserFromEventTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldDeleteUserFromEvent_whenUserIsOwner() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 1L;
            Long targetUserId = 2L;

            when(currentUserProvider.getUserId()).thenReturn(userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/events/{eventId}/users/{targetUserId}",
                            workspaceId, eventId, targetUserId))
                    .andExpect(status().isNoContent());

            verify(currentUserProvider).getUserId();
            verify(eventService).deleteUserFromEvent(workspaceId, eventId, targetUserId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotInWorkspace() throws Exception {

            Long userId = 99L;
            Long workspaceId = 1L;
            Long eventId = 1L;
            Long targetUserId = 2L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(eventService).deleteUserFromEvent(workspaceId, eventId, targetUserId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/events/{eventId}/users/{targetUserId}",
                            workspaceId, eventId, targetUserId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ACCESS_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).deleteUserFromEvent(workspaceId, eventId, targetUserId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotOwner() throws Exception {

            Long userId = 2L;
            Long workspaceId = 1L;
            Long eventId = 1L;
            Long targetUserId = 3L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceRoleDeniedException())
                    .when(eventService).deleteUserFromEvent(workspaceId, eventId, targetUserId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/events/{eventId}/users/{targetUserId}",
                            workspaceId, eventId, targetUserId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_ROLE_DENIED.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).deleteUserFromEvent(workspaceId, eventId, targetUserId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenWorkspaceDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 99L;
            Long eventId = 1L;
            Long targetUserId = 2L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new WorkspaceNotFoundException())
                    .when(eventService).deleteUserFromEvent(workspaceId, eventId, targetUserId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/events/{eventId}/users/{targetUserId}",
                            workspaceId, eventId, targetUserId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.WORKSPACE_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).deleteUserFromEvent(workspaceId, eventId, targetUserId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenEventDoesNotExist() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 99L;
            Long targetUserId = 2L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new EventNotFoundException())
                    .when(eventService).deleteUserFromEvent(workspaceId, eventId, targetUserId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/events/{eventId}/users/{targetUserId}",
                            workspaceId, eventId, targetUserId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.EVENT_NOT_FOUND.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).deleteUserFromEvent(workspaceId, eventId, targetUserId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn404_whenTargetUserIsNotAssignedToEvent() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 1L;
            Long targetUserId = 99L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new UserNotAssignedToEventException())
                    .when(eventService).deleteUserFromEvent(workspaceId, eventId, targetUserId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/events/{eventId}/users/{targetUserId}",
                            workspaceId, eventId, targetUserId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_ASSIGNED_TO_EVENT.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).deleteUserFromEvent(workspaceId, eventId, targetUserId, userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn409_whenEventIsNotAssignable() throws Exception {

            Long userId = 1L;
            Long workspaceId = 1L;
            Long eventId = 1L;
            Long targetUserId = 2L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new EventNotAssignableException())
                    .when(eventService).deleteUserFromEvent(workspaceId, eventId, targetUserId, userId);

            mockMvc.perform(delete("/api/workspaces/{workspaceId}/events/{eventId}/users/{targetUserId}",
                            workspaceId, eventId, targetUserId))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCode.EVENT_NOT_ASSIGNABLE.name()));

            verify(currentUserProvider).getUserId();
            verify(eventService).deleteUserFromEvent(workspaceId, eventId, targetUserId, userId);
        }
    }
}
