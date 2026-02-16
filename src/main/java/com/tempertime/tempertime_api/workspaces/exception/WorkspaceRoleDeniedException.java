package com.tempertime.tempertime_api.workspaces.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class WorkspaceRoleDeniedException extends RuntimeException {

    public WorkspaceRoleDeniedException() {
        super(ErrorCode.WORKSPACE_ROLE_DENIED.getDefaultMessage());
    }
}
