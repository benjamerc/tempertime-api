package com.tempertime.tempertime_api.workspaces.exception;

public class WorkspaceNotFoundException extends RuntimeException {
    public WorkspaceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkspaceNotFoundException(String message) {
        super(message);
    }
}
