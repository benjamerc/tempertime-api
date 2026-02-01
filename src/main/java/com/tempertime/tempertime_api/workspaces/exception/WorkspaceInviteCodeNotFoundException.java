package com.tempertime.tempertime_api.workspaces.exception;

public class WorkspaceInviteCodeNotFoundException extends RuntimeException {
    public WorkspaceInviteCodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkspaceInviteCodeNotFoundException(String message) {
        super(message);
    }
}
