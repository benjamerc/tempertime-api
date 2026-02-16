package com.tempertime.tempertime_api.workspaces.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class WorkspaceNotArchivedException extends RuntimeException {

    public WorkspaceNotArchivedException() {
        super(ErrorCode.WORKSPACE_NOT_ARCHIVED.getDefaultMessage());
    }
}
