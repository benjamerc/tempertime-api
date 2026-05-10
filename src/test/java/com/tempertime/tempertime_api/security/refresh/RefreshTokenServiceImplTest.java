package com.tempertime.tempertime_api.security.refresh;

import com.tempertime.tempertime_api.common.hash.Hash;

import com.tempertime.tempertime_api.security.config.JwtProperties;
import com.tempertime.tempertime_api.security.exception.RefreshTokenExpiredException;
import com.tempertime.tempertime_api.security.exception.RefreshTokenNotFoundException;
import com.tempertime.tempertime_api.security.exception.RefreshTokenRevokedException;
import com.tempertime.tempertime_api.users.data.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private JwtProperties.RefreshToken refreshTokenProperties;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Nested
    class CreateRefreshTokenTests {

        @Test
        void shouldCreateAndPersistRefreshToken_whenUserIsProvided() {

            User user = UserTestDataProvider.user(1L);

            when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenProperties);
            when(refreshTokenProperties.getExpiration()).thenReturn(86400000L);
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            String rawToken = refreshTokenService.createRefreshToken(user);

            assertThat(rawToken).isNotNull().isNotBlank();

            verify(refreshTokenRepository).save(argThat(token ->
                    token.getUser().equals(user) &&
                            token.getTokenHash() != null &&
                            token.getExpirationDate() != null
            ));
        }

        @Test
        void shouldReturnDifferentTokensOnSuccessiveCalls() {

            User user = UserTestDataProvider.user(1L);

            when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenProperties);
            when(refreshTokenProperties.getExpiration()).thenReturn(86400000L);
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            String first = refreshTokenService.createRefreshToken(user);
            String second = refreshTokenService.createRefreshToken(user);

            assertThat(first).isNotEqualTo(second);
        }
    }

    @Nested
    class ValidateRefreshTokenTests {

        @Test
        void shouldReturnRefreshToken_whenTokenIsValid() {

            User user = UserTestDataProvider.user(1L);
            String rawToken = UUID.randomUUID().toString();
            String hashedToken = Hash.sha256(rawToken);

            RefreshToken refreshToken = RefreshToken.builder()
                    .tokenHash(hashedToken)
                    .user(user)
                    .revoked(false)
                    .expirationDate(Instant.now().plusSeconds(3600))
                    .build();

            when(refreshTokenRepository.findByTokenHash(hashedToken))
                    .thenReturn(Optional.of(refreshToken));

            RefreshToken result = refreshTokenService.validateRefreshToken(rawToken);

            assertThat(result).isNotNull();
            assertThat(result.getTokenHash()).isEqualTo(hashedToken);

            verify(refreshTokenRepository).findByTokenHash(hashedToken);
        }

        @Test
        void shouldThrowRefreshTokenNotFoundException_whenTokenDoesNotExist() {

            String rawToken = UUID.randomUUID().toString();
            String hashedToken = Hash.sha256(rawToken);

            when(refreshTokenRepository.findByTokenHash(hashedToken))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.validateRefreshToken(rawToken))
                    .isInstanceOf(RefreshTokenNotFoundException.class);

            verify(refreshTokenRepository).findByTokenHash(hashedToken);
        }

        @Test
        void shouldThrowRefreshTokenRevokedException_whenTokenIsRevoked() {

            String rawToken = UUID.randomUUID().toString();
            String hashedToken = Hash.sha256(rawToken);

            RefreshToken refreshToken = RefreshToken.builder()
                    .tokenHash(hashedToken)
                    .revoked(true)
                    .expirationDate(Instant.now().plusSeconds(3600))
                    .build();

            when(refreshTokenRepository.findByTokenHash(hashedToken))
                    .thenReturn(Optional.of(refreshToken));

            assertThatThrownBy(() -> refreshTokenService.validateRefreshToken(rawToken))
                    .isInstanceOf(RefreshTokenRevokedException.class);
        }

        @Test
        void shouldThrowRefreshTokenExpiredException_whenTokenIsExpired() {

            String rawToken = UUID.randomUUID().toString();
            String hashedToken = Hash.sha256(rawToken);

            RefreshToken refreshToken = RefreshToken.builder()
                    .tokenHash(hashedToken)
                    .revoked(false)
                    .expirationDate(Instant.now().minusSeconds(3600))
                    .build();

            when(refreshTokenRepository.findByTokenHash(hashedToken))
                    .thenReturn(Optional.of(refreshToken));

            assertThatThrownBy(() -> refreshTokenService.validateRefreshToken(rawToken))
                    .isInstanceOf(RefreshTokenExpiredException.class);
        }
    }

    @Nested
    class RotateRefreshTokenTests {

        @Test
        void shouldRevokeOldTokenAndReturnNewOne() {

            User user = UserTestDataProvider.user(1L);

            RefreshToken oldToken = RefreshToken.builder()
                    .tokenHash("oldHash")
                    .user(user)
                    .revoked(false)
                    .expirationDate(Instant.now().plusSeconds(3600))
                    .build();

            when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenProperties);
            when(refreshTokenProperties.getExpiration()).thenReturn(86400000L);
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            String newRawToken = refreshTokenService.rotateRefreshToken(oldToken);

            assertThat(oldToken.getRevoked()).isTrue();
            assertThat(newRawToken).isNotNull().isNotBlank();

            verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
        }
    }

    @Nested
    class RevokeRefreshTokenTests {

        @Test
        void shouldRevokeToken_whenTokenIsValid() {

            String rawToken = UUID.randomUUID().toString();
            String hashedToken = Hash.sha256(rawToken);

            RefreshToken refreshToken = RefreshToken.builder()
                    .tokenHash(hashedToken)
                    .revoked(false)
                    .expirationDate(Instant.now().plusSeconds(3600))
                    .build();

            when(refreshTokenRepository.findByTokenHash(hashedToken))
                    .thenReturn(Optional.of(refreshToken));
            when(refreshTokenRepository.save(refreshToken)).thenReturn(refreshToken);

            refreshTokenService.revokeRefreshToken(rawToken);

            assertThat(refreshToken.getRevoked()).isTrue();

            verify(refreshTokenRepository).save(refreshToken);
        }

        @Test
        void shouldThrowRefreshTokenNotFoundException_whenTokenDoesNotExist() {

            String rawToken = UUID.randomUUID().toString();
            String hashedToken = Hash.sha256(rawToken);

            when(refreshTokenRepository.findByTokenHash(hashedToken))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.revokeRefreshToken(rawToken))
                    .isInstanceOf(RefreshTokenNotFoundException.class);

            verify(refreshTokenRepository, never()).save(any());
        }
    }

    @Nested
    class RevokeAllRefreshTokensForUserTests {

        @Test
        void shouldRevokeAllActiveTokens_whenUserHasActiveTokens() {

            User user = UserTestDataProvider.user(1L);

            RefreshToken token1 = RefreshToken.builder()
                    .revoked(false)
                    .expirationDate(Instant.now().plusSeconds(3600))
                    .build();

            RefreshToken token2 = RefreshToken.builder()
                    .revoked(false)
                    .expirationDate(Instant.now().plusSeconds(3600))
                    .build();

            when(refreshTokenRepository.findAllByUserAndRevokedFalse(user))
                    .thenReturn(List.of(token1, token2));
            when(refreshTokenRepository.saveAll(anyList()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            refreshTokenService.revokeAllRefreshTokensForUser(user);

            assertThat(token1.getRevoked()).isTrue();
            assertThat(token2.getRevoked()).isTrue();

            verify(refreshTokenRepository).saveAll(anyList());
        }

        @Test
        void shouldNotSave_whenUserHasNoActiveTokens() {

            User user = UserTestDataProvider.user(1L);

            when(refreshTokenRepository.findAllByUserAndRevokedFalse(user))
                    .thenReturn(List.of());

            refreshTokenService.revokeAllRefreshTokensForUser(user);

            verify(refreshTokenRepository, never()).saveAll(any());
        }
    }
}
