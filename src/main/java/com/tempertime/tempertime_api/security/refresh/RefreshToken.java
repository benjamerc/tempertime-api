package com.tempertime.tempertime_api.security.refresh;

import com.tempertime.tempertime_api.users.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Represents a refresh token used for obtaining new access tokens without
 * requiring the user to log in again. The token is hashed before storage
 * for security. Each token is associated with a single user.
 */
@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_tokens_user_id", columnList = "id_user")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SHA-256 hashed value of the refresh token, unique per token */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expiration_date", nullable = false)
    private Instant expirationDate;

    @Builder.Default
    @Column(nullable = false)
    private Boolean revoked = false;

    /** The user to whom this token belongs */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
