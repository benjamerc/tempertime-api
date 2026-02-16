package com.tempertime.tempertime_api.workspaces.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class WorkspaceOperationNotAllowedException extends RuntimeException {

    public WorkspaceOperationNotAllowedException() {
        super(ErrorCode.WORKSPACE_OPERATION_NOT_ALLOWED.getDefaultMessage());
    }
}
