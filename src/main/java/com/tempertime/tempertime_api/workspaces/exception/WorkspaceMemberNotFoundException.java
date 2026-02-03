package com.tempertime.tempertime_api.workspaces.exception;

public class WorkspaceMemberNotFoundException extends RuntimeException {
    public WorkspaceMemberNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkspaceMemberNotFoundException(String message) {
        super(message);
    }
}
