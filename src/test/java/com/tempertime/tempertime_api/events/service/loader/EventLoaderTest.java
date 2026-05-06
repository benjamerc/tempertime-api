package com.tempertime.tempertime_api.events.service.loader;

import com.tempertime.tempertime_api.events.EventTestDataProvider;
import com.tempertime.tempertime_api.events.domain.Event;
import com.tempertime.tempertime_api.events.exception.EventNotFoundException;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import com.tempertime.tempertime_api.workspaces.WorkspaceTestDataProvider;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventLoaderTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventLoader eventLoader;

    @Test
    void shouldReturnEvent_whenEventExistsInWorkspace() {

        Long workspaceId = 1L;
        Long eventId = 1L;

        Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
        Event event = EventTestDataProvider.event(eventId, workspace);

        when(eventRepository.findByIdAndWorkspaceId(eventId, workspaceId))
                .thenReturn(Optional.of(event));

        Event result = eventLoader.loadOrThrow(workspaceId, eventId);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(event);

        verify(eventRepository).findByIdAndWorkspaceId(eventId, workspaceId);
    }

    @Test
    void shouldThrowEventNotFoundException_whenEventDoesNotExistInWorkspace() {

        Long workspaceId = 1L;
        Long eventId = 99L;

        when(eventRepository.findByIdAndWorkspaceId(eventId, workspaceId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventLoader.loadOrThrow(workspaceId, eventId))
                .isInstanceOf(EventNotFoundException.class);

        verify(eventRepository).findByIdAndWorkspaceId(eventId, workspaceId);
    }
}
