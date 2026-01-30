package com.tempertime.tempertime_api.workspaces.exception;

public class WorkspaceRoleDeniedException extends RuntimeException {
    public WorkspaceRoleDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkspaceRoleDeniedException(String message) {
        super(message);
    }
}
