package com.tempertime.tempertime_api.events.controller;

import com.tempertime.tempertime_api.common.error.ApiErrorBuilder;
import com.tempertime.tempertime_api.common.error.GlobalExceptionHandler;
import com.tempertime.tempertime_api.common.error.model.ErrorCode;
import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.events.data.EventTestDataProvider;
import com.tempertime.tempertime_api.events.domain.Event;
import com.tempertime.tempertime_api.events.domain.EventPeriod;
import com.tempertime.tempertime_api.events.dto.response.UserEventResponse;
import com.tempertime.tempertime_api.events.exception.TimeZoneMissingException;
import com.tempertime.tempertime_api.events.service.core.UserEventService;
import com.tempertime.tempertime_api.security.core.CurrentUserProvider;
import com.tempertime.tempertime_api.security.jwt.AccessTokenService;
import com.tempertime.tempertime_api.security.jwt.JwtAuthenticationFilter;
import com.tempertime.tempertime_api.workspaces.data.WorkspaceTestDataProvider;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserEventController.class)
@Import({GlobalExceptionHandler.class, ApiErrorBuilder.class})
@AutoConfigureMockMvc(addFilters = false)
public class UserEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserEventService userEventService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @Nested
    class GetUserEventsTests {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnPagedEvents_whenPeriodIsAll() throws Exception {

            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            Event event = EventTestDataProvider.event(1L, workspace);
            UserEventResponse userEventResponse = EventTestDataProvider.userEventResponse(event);

            PageResponse<UserEventResponse> pageResponse = new PageResponse<>(
                    List.of(userEventResponse), 0, 20, 1L, 1, true
            );

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(userEventService.getUserEvents(eq(userId), any(), any(), any(), any()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/users/me/events")
                            .param("period", "ALL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(event.getId()))
                    .andExpect(jsonPath("$.content[0].title").value(event.getTitle()))
                    .andExpect(jsonPath("$.content[0].workspaceId").value(workspace.getId()))
                    .andExpect(jsonPath("$.content[0].workspaceName").value(workspace.getName()))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(currentUserProvider).getUserId();
            verify(userEventService).getUserEvents(eq(userId), any(), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnPagedEvents_whenPeriodIsDayAndTimeZoneProvided() throws Exception {

            Long userId = 1L;

            PageResponse<UserEventResponse> pageResponse = new PageResponse<>(
                    List.of(), 0, 20, 0L, 0, true
            );

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(userEventService.getUserEvents(eq(userId), eq(EventPeriod.DAY), any(), any(), any()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/users/me/events")
                            .param("period", "DAY")
                            .param("timeZone", "America/Argentina/Buenos_Aires"))
                    .andExpect(status().isOk());

            verify(currentUserProvider).getUserId();
            verify(userEventService).getUserEvents(eq(userId), eq(EventPeriod.DAY), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnEmptyPage_whenUserHasNoEvents() throws Exception {

            Long userId = 1L;

            PageResponse<UserEventResponse> pageResponse = new PageResponse<>(
                    List.of(), 0, 20, 0L, 0, true
            );

            when(currentUserProvider.getUserId()).thenReturn(userId);
            when(userEventService.getUserEvents(eq(userId), any(), any(), any(), any()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/users/me/events")
                            .param("period", "ALL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(currentUserProvider).getUserId();
            verify(userEventService).getUserEvents(eq(userId), any(), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturn400_whenTimeZoneIsMissing() throws Exception {

            Long userId = 1L;

            when(currentUserProvider.getUserId()).thenReturn(userId);
            doThrow(new TimeZoneMissingException())
                    .when(userEventService).getUserEvents(eq(userId), any(), any(), any(), any());

            mockMvc.perform(get("/api/users/me/events")
                            .param("period", "DAY"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.TIME_ZONE_MISSING.name()));

            verify(currentUserProvider).getUserId();
            verify(userEventService).getUserEvents(eq(userId), any(), any(), any(), any());
        }
    }
}
