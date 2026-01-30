package com.tempertime.tempertime_api.workspaces.exception;

public class WorkspaceNotArchivedException extends RuntimeException {
    public WorkspaceNotArchivedException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkspaceNotArchivedException(String message) {
        super(message);
    }
}
