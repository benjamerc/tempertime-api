package com.tempertime.tempertime_api.workspaces.controller;

import com.tempertime.tempertime_api.security.core.CurrentUserProvider;
import com.tempertime.tempertime_api.security.core.CustomUserDetails;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceCreateRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceJoinRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceUpdateRequest;
import com.tempertime.tempertime_api.workspaces.dto.response.*;
import com.tempertime.tempertime_api.workspaces.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final CurrentUserProvider  currentUserProvider;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceCreateResponse> createWorkspace(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WorkspaceCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(workspaceService.createWorkspace(
                        request,
                        currentUserProvider.getUserId(userDetails)
                ));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WorkspaceListItemResponse>> getUserWorkspaces(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(workspaceService.getUserWorkspaces(currentUserProvider.getUserId(userDetails)));
    }

    @GetMapping("/{workspaceId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceDetailResponse> getWorkspaceById(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(workspaceService.getWorkspaceById(
                workspaceId,
                currentUserProvider.getUserId(userDetails)
        ));
    }

    @PatchMapping("/{workspaceId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceUpdateResponse> updateWorkspace(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WorkspaceUpdateRequest request) {

        return ResponseEntity.ok(workspaceService.updateWorkspace(
                workspaceId,
                currentUserProvider.getUserId(userDetails),
                request
        ));
    }

    @PatchMapping("/{workspaceId}/archive")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> archiveWorkspace(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        workspaceService.archiveWorkspace(
                workspaceId,
                currentUserProvider.getUserId(userDetails)
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{workspaceId}/unarchive")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> unarchiveWorkspace(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        workspaceService.unarchiveWorkspace(
                workspaceId,
                currentUserProvider.getUserId(userDetails)
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{workspaceId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteWorkspace(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        workspaceService.deleteWorkspace(
                workspaceId,
                currentUserProvider.getUserId(userDetails)
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{workspaceId}/invite-code")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceInviteCodeResponse> getInviteCode(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                workspaceService.getInviteCode(
                        workspaceId,
                        currentUserProvider.getUserId(userDetails)
                )
        );
    }

    @PatchMapping("/{workspaceId}/invite-code/enable")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceInviteCodeResponse> activateInviteCode(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                workspaceService.activateInviteCode(
                        workspaceId,
                        currentUserProvider.getUserId(userDetails)
                )
        );
    }

    @PatchMapping("/{workspaceId}/invite-code/disable")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceInviteCodeResponse> deactivateInviteCode(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                workspaceService.deactivateInviteCode(
                        workspaceId,
                        currentUserProvider.getUserId(userDetails)
                )
        );
    }

    @PatchMapping("/{workspaceId}/invite-code/regenerate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceInviteCodeRegenerateResponse> regenerateInviteCode(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                workspaceService.regenerateInviteCode(
                        workspaceId,
                        currentUserProvider.getUserId(userDetails)
                )
        );
    }

    @PostMapping("/join")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceJoinResponse> joinWorkspace(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WorkspaceJoinRequest request
    ) {
        return ResponseEntity.ok(
                workspaceService.joinWorkspace(
                        request.inviteCode(),
                        currentUserProvider.getUserId(userDetails)
                )
        );
    }

    @GetMapping("/{workspaceId}/members")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WorkspaceMemberResponse>> getWorkspaceUsers(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                workspaceService.getWorkspaceUsers(
                        workspaceId,
                        currentUserProvider.getUserId(userDetails)
                )
        );
    }

    @DeleteMapping("/{workspaceId}/members/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeWorkspaceUser(
            @PathVariable Long workspaceId,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        workspaceService.removeWorkspaceUser(
                workspaceId,
                userId,
                currentUserProvider.getUserId(userDetails)
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{workspaceId}/members/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> leaveWorkspace(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        workspaceService.leaveWorkspace(
                workspaceId,
                currentUserProvider.getUserId(userDetails)
        );

        return ResponseEntity.noContent().build();
    }
}
