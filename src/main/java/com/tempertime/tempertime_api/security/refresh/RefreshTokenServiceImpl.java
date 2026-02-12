package com.tempertime.tempertime_api.security.refresh;

import com.tempertime.tempertime_api.security.config.JwtProperties;
import com.tempertime.tempertime_api.security.exception.RefreshTokenExpiredException;
import com.tempertime.tempertime_api.security.exception.RefreshTokenNotFoundException;
import com.tempertime.tempertime_api.security.exception.RefreshTokenRevokedException;
import com.tempertime.tempertime_api.common.hash.Hash;
import com.tempertime.tempertime_api.users.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Refresh token lifecycle implementation */
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    /** Creates and persists a new refresh token for a user */
    @Override
    public String createRefreshToken(User user) {

        long expirationMillis = jwtProperties.getRefreshToken().getExpiration();

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = hash(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(hashedToken)
                .user(user)
                .expirationDate(Instant.now().plusMillis(expirationMillis))
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    /**
     * Validates a refresh token:
     * - Exists in DB
     * - Not revoked
     * - Not expired
     */
    @Override
    public RefreshToken validateRefreshToken(String rawToken) {

        RefreshToken refreshToken = getRefreshTokenOrThrow(hash(rawToken));

        if (refreshToken.getRevoked()) {
            throw new RefreshTokenRevokedException("Refresh token revoked");
        }

        if (refreshToken.getExpirationDate().isBefore(Instant.now())) {
            throw new RefreshTokenExpiredException("Refresh token expired");
        }

        return refreshToken;
    }

    /**
     * Rotates a refresh token: revokes the current one and creates a new token for the same user.
     */
    @Override
    public String rotateRefreshToken(RefreshToken refreshToken) {

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return createRefreshToken(refreshToken.getUser());
    }

    @Override
    public void revokeRefreshToken(String rawToken) {

        RefreshToken refreshToken = validateRefreshToken(rawToken);

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void revokeAllRefreshTokensForUser(User user) {

        List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUserAndRevokedFalse(user);

        if (!refreshTokens.isEmpty()) {
            refreshTokens.forEach(t -> t.setRevoked(true));
            refreshTokenRepository.saveAll(refreshTokens);
        }
    }

    /**
     * Retrieves a refresh token from DB or throws an exception if not found.
     */
    private RefreshToken getRefreshTokenOrThrow(String hashedToken) {

        return refreshTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found"));
    }

    /** Hashes the raw token using SHA-256 */
    private String hash(String rawToken) {
        return Hash.sha256(rawToken);
    }
}
