package com.tempertime.tempertime_api.workspaces.exception;

public class WorkspaceOperationNotAllowedException extends RuntimeException {
    public WorkspaceOperationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkspaceOperationNotAllowedException(String message) {
        super(message);
    }
}
