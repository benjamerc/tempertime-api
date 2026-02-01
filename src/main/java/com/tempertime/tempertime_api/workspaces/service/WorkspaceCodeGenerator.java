package com.tempertime.tempertime_api.workspaces.service;

import com.tempertime.tempertime_api.workspaces.exception.WorkspaceInviteCodeGenerationException;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * Generates unique invitation codes for workspaces.
 * Ensures uniqueness against persisted codes.
 */
@Service
@RequiredArgsConstructor
public class WorkspaceCodeGenerator {

    private static final int CODE_LENGTH = 8;
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int MAX_ATTEMPTS = 10;

    private final SecureRandom random = new SecureRandom();
    private final WorkspaceCodeRepository workspaceCodeRepository;

    /** Generates a unique invite code for a workspace */
    public String generate() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String code = randomCode();
            if (workspaceCodeRepository.findByCode(code).isEmpty()) {
                return code;
            }
        }
        throw new WorkspaceInviteCodeGenerationException("Unable to generate unique workspace invite code after multiple attempts");
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }
}
