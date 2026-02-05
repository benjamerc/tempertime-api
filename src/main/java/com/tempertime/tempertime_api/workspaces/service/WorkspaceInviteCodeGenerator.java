package com.tempertime.tempertime_api.workspaces.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Generates workspace invite codes */
@Component
public class WorkspaceInviteCodeGenerator {

    // Length of the workspace invite code
    private static final int CODE_LENGTH = 12;

    // Characters allowed in the invite code
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }
}
