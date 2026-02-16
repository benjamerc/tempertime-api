package com.tempertime.tempertime_api.workspaces.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class WorkspaceInviteCodeNotFoundException extends RuntimeException {

    public WorkspaceInviteCodeNotFoundException() {
        super(ErrorCode.WORKSPACE_INVITE_CODE_NOT_FOUND.getDefaultMessage());
    }
}
