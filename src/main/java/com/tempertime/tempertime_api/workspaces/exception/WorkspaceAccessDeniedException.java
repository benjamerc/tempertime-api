package com.tempertime.tempertime_api.workspaces.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class WorkspaceAccessDeniedException extends RuntimeException {

    public WorkspaceAccessDeniedException() {
        super(ErrorCode.WORKSPACE_ACCESS_DENIED.getDefaultMessage());
    }

    public WorkspaceAccessDeniedException(String message) {
        super(message);
    }
}
