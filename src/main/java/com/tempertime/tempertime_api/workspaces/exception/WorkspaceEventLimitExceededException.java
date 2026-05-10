package com.tempertime.tempertime_api.workspaces.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class WorkspaceEventLimitExceededException extends RuntimeException {

    public WorkspaceEventLimitExceededException() {
        super(ErrorCode.WORKSPACE_EVENT_LIMIT_EXCEEDED.getDefaultMessage());
    }
}
