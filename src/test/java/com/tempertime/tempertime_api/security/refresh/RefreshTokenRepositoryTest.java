package com.tempertime.tempertime_api.security.refresh;

import com.tempertime.tempertime_api.users.data.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@Transactional
public class RefreshTokenRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(UserTestDataProvider.user(null));
    }

    private RefreshToken buildToken(User user, boolean revoked) {
        return RefreshToken.builder()
                .tokenHash(UUID.randomUUID().toString())
                .user(user)
                .revoked(revoked)
                .expirationDate(Instant.now().plusSeconds(3600))
                .build();
    }

    @Nested
    class FindByTokenHashTests {

        @Test
        void shouldReturnRefreshToken_whenHashExists() {

            RefreshToken saved = refreshTokenRepository.save(buildToken(savedUser, false));

            Optional<RefreshToken> result =
                    refreshTokenRepository.findByTokenHash(saved.getTokenHash());

            assertThat(result).isPresent();
            assertThat(result.get().getTokenHash()).isEqualTo(saved.getTokenHash());
        }

        @Test
        void shouldReturnEmpty_whenHashDoesNotExist() {

            Optional<RefreshToken> result =
                    refreshTokenRepository.findByTokenHash("nonexistenthash");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindAllByUserAndRevokedFalseTests {

        @Test
        void shouldReturnActiveTokens_whenUserHasActiveTokens() {

            refreshTokenRepository.save(buildToken(savedUser, false));
            refreshTokenRepository.save(buildToken(savedUser, false));
            refreshTokenRepository.save(buildToken(savedUser, true));

            List<RefreshToken> result =
                    refreshTokenRepository.findAllByUserAndRevokedFalse(savedUser);

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(t -> !t.getRevoked());
        }

        @Test
        void shouldReturnEmptyList_whenUserHasNoActiveTokens() {

            refreshTokenRepository.save(buildToken(savedUser, true));
            refreshTokenRepository.save(buildToken(savedUser, true));

            List<RefreshToken> result =
                    refreshTokenRepository.findAllByUserAndRevokedFalse(savedUser);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnOnlyTokensForGivenUser_whenMultipleUsersExist() {

            User otherUser = userRepository.save(UserTestDataProvider.user(null));

            refreshTokenRepository.save(buildToken(savedUser, false));
            refreshTokenRepository.save(buildToken(otherUser, false));

            List<RefreshToken> result =
                    refreshTokenRepository.findAllByUserAndRevokedFalse(savedUser);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUser().getId()).isEqualTo(savedUser.getId());
        }
    }
}
