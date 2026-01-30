package com.tempertime.tempertime_api.workspaces.exception;

public class WorkspaceAccessDeniedException extends RuntimeException {
    public WorkspaceAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkspaceAccessDeniedException(String message) {
        super(message);
    }
}
