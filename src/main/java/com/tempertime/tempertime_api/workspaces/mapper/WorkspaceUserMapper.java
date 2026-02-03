package com.tempertime.tempertime_api.workspaces.mapper;

import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceDetailResponse;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceJoinResponse;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceListItemResponse;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceMemberResponse;
import com.tempertime.tempertime_api.workspaces.model.WorkspaceUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/** Maps WorkspaceUser domain models to user-scoped workspace DTOs */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface WorkspaceUserMapper {

    @Mapping(target = "id", source = "workspace.id")
    @Mapping(target = "name", source = "workspace.name")
    @Mapping(target = "color", source = "workspace.color")
    @Mapping(target = "userRole", source = "role")
    @Mapping(target = "createdAt", source = "workspace.createdAt")
    @Mapping(target = "archived", source = "workspace.archived")
    WorkspaceDetailResponse toWorkspaceDetailResponse(WorkspaceUser workspaceUser);

    @Mapping(target = "id", source = "workspace.id")
    @Mapping(target = "name", source = "workspace.name")
    @Mapping(target = "color", source = "workspace.color")
    @Mapping(target = "archived", source = "workspace.archived")
    @Mapping(target = "userRole", source = "role")
    WorkspaceListItemResponse toWorkspaceListItemResponse(WorkspaceUser workspaceUser);

    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "role", source = "role")
    WorkspaceJoinResponse toWorkspaceJoinResponse(WorkspaceUser workspaceUser);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "role", source = "role")
    WorkspaceMemberResponse toWorkspaceMemberResponse(WorkspaceUser workspaceUser);
}
