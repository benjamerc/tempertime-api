package com.tempertime.tempertime_api.workspaces.controller;

import com.tempertime.tempertime_api.security.core.CustomUserDetails;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceCreateRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceUpdateRequest;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceDetailResponse;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceListItemResponse;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceResponse;
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

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WorkspaceCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(workspaceService.createWorkspace(request, userDetails.getUser().getId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WorkspaceListItemResponse>> getUserWorkspaces(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(workspaceService.getUserWorkspaces(userDetails.getUser().getId()));
    }

    @GetMapping("/{workspaceId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceDetailResponse> getWorkspaceById(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(workspaceService.getWorkspaceById(workspaceId, userDetails.getUser().getId()));
    }

    @PatchMapping("/{workspaceId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WorkspaceUpdateRequest request) {

        return ResponseEntity.ok(workspaceService.updateWorkspace(workspaceId, userDetails.getUser().getId(), request));
    }

    @PatchMapping("/{workspaceId}/archive")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> archiveWorkspace(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        workspaceService.archiveWorkspace(workspaceId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{workspaceId}/unarchive")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> unarchiveWorkspace(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        workspaceService.unarchiveWorkspace(workspaceId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{workspaceId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteWorkspace(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        workspaceService.deleteWorkspace(workspaceId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
