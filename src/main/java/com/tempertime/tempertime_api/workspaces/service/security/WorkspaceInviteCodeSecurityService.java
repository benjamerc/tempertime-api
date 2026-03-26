package com.tempertime.tempertime_api.workspaces.service.security;

public interface WorkspaceInviteCodeSecurityService {

    String encrypt(String rawInviteCode);

    String decrypt(String encryptedInviteCode);
}
