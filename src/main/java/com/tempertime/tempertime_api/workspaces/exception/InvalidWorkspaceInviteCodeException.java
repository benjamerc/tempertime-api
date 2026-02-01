package com.tempertime.tempertime_api.workspaces.exception;

public class InvalidWorkspaceInviteCodeException extends RuntimeException {
    public InvalidWorkspaceInviteCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidWorkspaceInviteCodeException(String message) {
        super(message);
    }
}
