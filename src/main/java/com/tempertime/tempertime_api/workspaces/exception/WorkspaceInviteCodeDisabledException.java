package com.tempertime.tempertime_api.workspaces.exception;

public class WorkspaceInviteCodeDisabledException extends RuntimeException {
    public WorkspaceInviteCodeDisabledException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkspaceInviteCodeDisabledException(String message) {
        super(message);
    }
}
