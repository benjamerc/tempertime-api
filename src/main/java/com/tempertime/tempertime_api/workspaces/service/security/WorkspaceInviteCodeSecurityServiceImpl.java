package com.tempertime.tempertime_api.workspaces.service.security;

import com.tempertime.tempertime_api.workspaces.config.WorkspaceInviteCodeProperties;
import com.tempertime.tempertime_api.workspaces.exception.InvalidEncryptedInviteCodeException;
import com.tempertime.tempertime_api.workspaces.exception.InviteCodeCryptoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for encrypting and decrypting workspace invite codes using AES/GCM.
 */
@Service
@RequiredArgsConstructor
public class WorkspaceInviteCodeSecurityServiceImpl implements WorkspaceInviteCodeSecurityService {

    private final WorkspaceInviteCodeProperties properties;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH = 128;

    /**
     * Returns the AES secret key decoded from Base64 configuration.
     */
    private SecretKeySpec getSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(properties.getSecretKey());
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * Encrypts the raw workspace invite code.
     *
     * @param rawInviteCode the code to encrypt
     * @return Base64 string containing IV + ciphertext
     * @throws InviteCodeCryptoException if encryption fails
     */
    @Override
    public String encrypt(String rawInviteCode) {
        try {
            byte[] iv = new byte[IV_SIZE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), spec);

            byte[] encryptedBytes = cipher.doFinal(rawInviteCode.getBytes());

            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new InviteCodeCryptoException("Error encrypting invite code", e);
        }
    }

    /**
     * Decrypts a Base64-encoded workspace invite code.
     *
     * @param encryptedInviteCode Base64 string with IV + ciphertext
     * @return the decrypted invite code
     * @throws InvalidEncryptedInviteCodeException if the input is tampered or corrupted
     * @throws InviteCodeCryptoException if a decryption error occurs due to system issues
     */
    @Override
    public String decrypt(String encryptedInviteCode) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedInviteCode);

            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(combined, 0, iv, 0, IV_SIZE);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);

            int ciphertextLength = combined.length - IV_SIZE;
            byte[] ciphertext = new byte[ciphertextLength];
            System.arraycopy(combined, IV_SIZE, ciphertext, 0, ciphertextLength);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec);

            byte[] decryptedBytes = cipher.doFinal(ciphertext);
            return new String(decryptedBytes);

        } catch (AEADBadTagException e) {
            throw new InvalidEncryptedInviteCodeException("Encrypted invite code is invalid or tampered", e);
        } catch (Exception e) {
            throw new InviteCodeCryptoException("Error decrypting invite code", e);
        }
    }
}