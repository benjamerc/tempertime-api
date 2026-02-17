package com.tempertime.tempertime_api.workspaces.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class WorkspaceCapacityExceededException extends RuntimeException {

    public WorkspaceCapacityExceededException() {
        super(ErrorCode.WORKSPACE_CAPACITY_EXCEEDED.getDefaultMessage());
    }
}
