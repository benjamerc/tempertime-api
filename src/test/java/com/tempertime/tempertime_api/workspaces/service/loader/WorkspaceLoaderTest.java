package com.tempertime.tempertime_api.workspaces.service.loader;

import com.tempertime.tempertime_api.workspaces.data.WorkspaceTestDataProvider;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceNotFoundException;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkspaceLoaderTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private WorkspaceLoader workspaceLoader;

    @Test
    void shouldReturnWorkspace_whenWorkspaceExists() {

        Long workspaceId = 1L;
        Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));

        Workspace result = workspaceLoader.loadOrThrow(workspaceId);

        assertThat(result).isEqualTo(workspace);

        verify(workspaceRepository).findById(workspaceId);
    }

    @Test
    void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

        Long workspaceId = 99L;

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceLoader.loadOrThrow(workspaceId))
                .isInstanceOf(WorkspaceNotFoundException.class);

        verify(workspaceRepository).findById(workspaceId);
    }
}
