package com.tempertime.tempertime_api.workspaces.exception;

public class WorkspaceInviteCodeGenerationException extends RuntimeException {
    public WorkspaceInviteCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkspaceInviteCodeGenerationException(String message) {
        super(message);
    }
}
