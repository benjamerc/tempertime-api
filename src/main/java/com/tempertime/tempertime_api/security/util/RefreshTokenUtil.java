package com.tempertime.tempertime_api.security.util;

import com.tempertime.tempertime_api.security.exception.HashingException;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@UtilityClass
public class RefreshTokenUtil {

    /** Hashes a refresh token using SHA-256 (hex encoded) */
    public String hashSHA256(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(64);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new HashingException("Error hashing refresh token", e);
        }
    }
}
