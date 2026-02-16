package com.tempertime.tempertime_api.workspaces.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class WorkspaceUserNotFoundException extends RuntimeException {

    public WorkspaceUserNotFoundException() {
        super(ErrorCode.WORKSPACE_USER_NOT_FOUND.getDefaultMessage());
    }
}
