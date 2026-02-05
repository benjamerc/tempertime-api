package com.tempertime.tempertime_api.workspaces.exception;

public class WorkspaceUserNotFoundException extends RuntimeException {
    public WorkspaceUserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkspaceUserNotFoundException(String message) {
        super(message);
    }
}
