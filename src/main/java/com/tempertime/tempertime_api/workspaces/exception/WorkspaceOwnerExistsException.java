package com.tempertime.tempertime_api.workspaces.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class WorkspaceOwnerExistsException extends RuntimeException {

    public WorkspaceOwnerExistsException() {
        super(ErrorCode.WORKSPACE_OWNER_RESTRICTION.getDefaultMessage());
    }
}
