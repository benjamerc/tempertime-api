package com.tempertime.tempertime_api.security.refresh;

import com.tempertime.tempertime_api.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /** Find a refresh token by its hashed value */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Get all active (non-revoked) tokens for a user */
    List<RefreshToken> findAllByUserAndRevokedFalse(User user);

    /** Active tokens for a user that expire after the given instant */
    List<RefreshToken> findAllByUserAndRevokedFalseAndExpirationDateAfter(
            User user,
            Instant now
    );
}
