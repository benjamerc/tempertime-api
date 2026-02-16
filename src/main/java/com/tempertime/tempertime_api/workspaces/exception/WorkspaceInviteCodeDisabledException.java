package com.tempertime.tempertime_api.workspaces.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class WorkspaceInviteCodeDisabledException extends RuntimeException {

    public WorkspaceInviteCodeDisabledException() {
        super(ErrorCode.WORKSPACE_INVITE_CODE_DISABLED.getDefaultMessage());
    }
}
