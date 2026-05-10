package com.tempertime.tempertime_api.security.jwt;

import com.tempertime.tempertime_api.security.config.JwtProperties;
import com.tempertime.tempertime_api.users.data.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;

import com.tempertime.tempertime_api.users.domain.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccessTokenServiceImplTest {

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AccessTokenServiceImpl accessTokenService;

    private static final String SECRET_KEY =
            "mySecretKeyForTestingPurposesOnlyThatIsLongEnough1234567890";

    @Nested
    class CreateAccessTokenTests {

        @Test
        void shouldCreateValidJwt_whenUserIsProvided() {

            User user = UserTestDataProvider.user(1L);

            when(jwtProperties.getSecretKey()).thenReturn(SECRET_KEY);
            when(jwtProperties.getExpiration()).thenReturn(3600000L);

            String token = accessTokenService.createAccessToken(user);

            assertThat(token).isNotNull().isNotBlank();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        void shouldIncludeCorrectClaims_whenTokenIsCreated() {

            User user = UserTestDataProvider.user(1L);

            when(jwtProperties.getSecretKey()).thenReturn(SECRET_KEY);
            when(jwtProperties.getExpiration()).thenReturn(3600000L);

            String token = accessTokenService.createAccessToken(user);
            Claims claims = accessTokenService.validateAccessToken(token);

            assertThat(claims.getSubject()).isEqualTo(user.getEmail());
            assertThat(claims.get("id", Long.class)).isEqualTo(user.getId());
            assertThat(claims.get("role", String.class)).isEqualTo(UserRole.USER.name());
        }
    }

    @Nested
    class ValidateAccessTokenTests {

        @Test
        void shouldReturnClaims_whenTokenIsValid() {

            User user = UserTestDataProvider.user(1L);

            when(jwtProperties.getSecretKey()).thenReturn(SECRET_KEY);
            when(jwtProperties.getExpiration()).thenReturn(3600000L);

            String token = accessTokenService.createAccessToken(user);
            Claims claims = accessTokenService.validateAccessToken(token);

            assertThat(claims).isNotNull();
            assertThat(claims.getSubject()).isEqualTo(user.getEmail());
        }

        @Test
        void shouldThrowException_whenTokenIsExpired() {

            User user = UserTestDataProvider.user(1L);

            when(jwtProperties.getSecretKey()).thenReturn(SECRET_KEY);
            when(jwtProperties.getExpiration()).thenReturn(-1000L);

            String token = accessTokenService.createAccessToken(user);

            assertThatThrownBy(() -> accessTokenService.validateAccessToken(token))
                    .isInstanceOf(Exception.class);
        }

        @Test
        void shouldThrowException_whenTokenIsInvalid() {

            when(jwtProperties.getSecretKey()).thenReturn(SECRET_KEY);

            assertThatThrownBy(() -> accessTokenService.validateAccessToken("invalid.token.here"))
                    .isInstanceOf(Exception.class);
        }

        @Test
        void shouldThrowException_whenTokenIsSignedWithDifferentKey() {

            User user = UserTestDataProvider.user(1L);
            String otherKey = "otherSecretKeyForTestingPurposesOnlyThatIsLongEnough1234567890";

            when(jwtProperties.getSecretKey()).thenReturn(SECRET_KEY);
            when(jwtProperties.getExpiration()).thenReturn(3600000L);

            String token = accessTokenService.createAccessToken(user);

            when(jwtProperties.getSecretKey()).thenReturn(otherKey);

            assertThatThrownBy(() -> accessTokenService.validateAccessToken(token))
                    .isInstanceOf(Exception.class);
        }
    }
}
