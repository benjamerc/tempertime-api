package com.tempertime.tempertime_api.workspaces.exception;

public class UserAlreadyInWorkspaceException extends RuntimeException {
    public UserAlreadyInWorkspaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserAlreadyInWorkspaceException(String message) {
        super(message);
    }
}
