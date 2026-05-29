package com.tempertime.tempertime_api.workspaces.controller;

import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.security.core.CurrentUserProvider;
import com.tempertime.tempertime_api.workspaces.controller.docs.WorkspaceControllerDocs;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceCreateRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceJoinRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceUpdateRequest;
import com.tempertime.tempertime_api.workspaces.dto.response.*;
import com.tempertime.tempertime_api.workspaces.service.core.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController implements WorkspaceControllerDocs {

    private final WorkspaceService workspaceService;
    private final CurrentUserProvider  currentUserProvider;

    @Override
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceCreateResponse> createWorkspace(
            @Valid @RequestBody WorkspaceCreateRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        workspaceService.createWorkspace(
                                request,
                                currentUserProvider.getUserId()
                        )
                );
    }

    @Override
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PageResponse<WorkspaceListItemResponse>> getUserWorkspaces(
            @RequestParam(required = false) WorkspaceRole role,
            @RequestParam(required = false) Boolean archived,
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "workspace.name",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                workspaceService.getUserWorkspaces(
                        currentUserProvider.getUserId(),
                        role,
                        archived,
                        pageable
                )
        );
    }

    @Override
    @GetMapping("/{workspaceId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceDetailResponse> getWorkspaceById(
            @PathVariable Long workspaceId
    ) {

        return ResponseEntity.ok(
                workspaceService.getWorkspaceById(
                        workspaceId,
                        currentUserProvider.getUserId()
                )
        );
    }

    @Override
    @PatchMapping("/{workspaceId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceUpdateResponse> updateWorkspace(
            @PathVariable Long workspaceId,
            @Valid @RequestBody WorkspaceUpdateRequest request
    ) {

        return ResponseEntity.ok(
                workspaceService.updateWorkspace(
                        workspaceId,
                        currentUserProvider.getUserId(),
                        request
                )
        );
    }

    @Override
    @PatchMapping("/{workspaceId}/archive")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> archiveWorkspace(
            @PathVariable Long workspaceId
    ) {

        workspaceService.archiveWorkspace(
                workspaceId,
                currentUserProvider.getUserId()
        );

        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{workspaceId}/unarchive")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> unarchiveWorkspace(
            @PathVariable Long workspaceId
    ) {

        workspaceService.unarchiveWorkspace(
                workspaceId,
                currentUserProvider.getUserId()
        );

        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{workspaceId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteWorkspace(
            @PathVariable Long workspaceId
    ) {

        workspaceService.deleteWorkspace(
                workspaceId,
                currentUserProvider.getUserId()
        );

        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{workspaceId}/invite-code")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceInviteCodeResponse> getInviteCode(
            @PathVariable Long workspaceId
    ) {

        return ResponseEntity.ok(
                workspaceService.getInviteCode(
                        workspaceId,
                        currentUserProvider.getUserId()
                )
        );
    }

    @Override
    @PatchMapping("/{workspaceId}/invite-code/enable")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceInviteCodeStatusResponse> activateInviteCode(
            @PathVariable Long workspaceId
    ) {

        return ResponseEntity.ok(
                workspaceService.activateInviteCode(
                        workspaceId,
                        currentUserProvider.getUserId()
                )
        );
    }

    @Override
    @PatchMapping("/{workspaceId}/invite-code/disable")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceInviteCodeStatusResponse> deactivateInviteCode(
            @PathVariable Long workspaceId
    ) {

        return ResponseEntity.ok(
                workspaceService.deactivateInviteCode(
                        workspaceId,
                        currentUserProvider.getUserId()
                )
        );
    }

    @Override
    @PatchMapping("/{workspaceId}/invite-code/regenerate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceInviteCodeResponse> regenerateInviteCode(
            @PathVariable Long workspaceId
    ) {

        return ResponseEntity.ok(
                workspaceService.regenerateInviteCode(
                        workspaceId,
                        currentUserProvider.getUserId()
                )
        );
    }

    @Override
    @PostMapping("/join")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceJoinResponse> joinWorkspace(
            @Valid @RequestBody WorkspaceJoinRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        workspaceService.joinWorkspace(
                                request.inviteCode(),
                                currentUserProvider.getUserId()
                        )
                );
    }

    @Override
    @GetMapping("/{workspaceId}/users")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PageResponse<WorkspaceUserResponse>> getWorkspaceUsers(
            @PathVariable Long workspaceId,
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "user.firstName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                workspaceService.getWorkspaceUsers(
                        workspaceId,
                        currentUserProvider.getUserId(),
                        pageable
                )
        );
    }

    @Override
    @DeleteMapping("/{workspaceId}/users/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeWorkspaceUser(
            @PathVariable Long workspaceId,
            @PathVariable("userId") Long targetUserId
    ) {

        workspaceService.removeWorkspaceUser(
                workspaceId,
                targetUserId,
                currentUserProvider.getUserId()
        );

        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{workspaceId}/users/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> leaveWorkspace(
            @PathVariable Long workspaceId
    ) {

        workspaceService.leaveWorkspace(
                workspaceId,
                currentUserProvider.getUserId()
        );

        return ResponseEntity.noContent().build();
    }
}
