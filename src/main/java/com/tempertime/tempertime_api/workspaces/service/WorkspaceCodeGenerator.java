package com.tempertime.tempertime_api.workspaces.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Generates invitation codes for workspaces */
@Component
public class WorkspaceCodeGenerator {

    private static final int CODE_LENGTH = 12;
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
