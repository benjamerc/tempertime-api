package com.tempertime.tempertime_api.workspaces.exception;

import com.tempertime.tempertime_api.common.error.model.ErrorCode;

public class InvalidWorkspaceInviteCodeException extends RuntimeException {

    public InvalidWorkspaceInviteCodeException() {
        super(ErrorCode.INVALID_WORKSPACE_INVITE_CODE.getDefaultMessage());
    }
}
