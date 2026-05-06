package com.tempertime.tempertime_api.auth.service;

import com.tempertime.tempertime_api.auth.dto.request.AuthLoginRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRefreshTokenRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRegisterRequest;
import com.tempertime.tempertime_api.auth.dto.response.AuthRegisterResponse;
import com.tempertime.tempertime_api.auth.dto.response.AuthTokenResponse;
import com.tempertime.tempertime_api.auth.exception.EmailAlreadyExistsException;
import com.tempertime.tempertime_api.auth.mapper.AuthMapper;
import com.tempertime.tempertime_api.common.normalizer.InputNormalizer;
import com.tempertime.tempertime_api.common.validator.InvalidPasswordFormatException;
import com.tempertime.tempertime_api.common.validator.PasswordValidator;
import com.tempertime.tempertime_api.security.core.CustomUserDetails;
import com.tempertime.tempertime_api.security.exception.RefreshTokenExpiredException;
import com.tempertime.tempertime_api.security.exception.RefreshTokenNotFoundException;
import com.tempertime.tempertime_api.security.exception.RefreshTokenRevokedException;
import com.tempertime.tempertime_api.security.jwt.AccessTokenService;
import com.tempertime.tempertime_api.security.refresh.RefreshToken;
import com.tempertime.tempertime_api.security.refresh.RefreshTokenService;
import com.tempertime.tempertime_api.users.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AccessTokenService accessTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private InputNormalizer inputNormalizer;

    @InjectMocks
    private AuthServiceImpl authService;

    @Nested
    class RegisterTests {

        @Test
        void shouldRegisterUser_whenValidDataProvided() {

            AuthRegisterRequest request = new AuthRegisterRequest(
                    "test@mail.com", "John", "Doe", "Password123"
            );

            User user = UserTestDataProvider.user(1L);
            AuthRegisterResponse response = new AuthRegisterResponse(user.getEmail());

            when(userRepository.existsByEmail(request.email())).thenReturn(false);
            doNothing().when(passwordValidator).validate(request.password());
            when(inputNormalizer.normalize(request.email())).thenReturn(request.email());
            when(inputNormalizer.normalize(request.firstName())).thenReturn(request.firstName());
            when(inputNormalizer.normalize(request.lastName())).thenReturn(request.lastName());
            when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(authMapper.toAuthRegisterResponse(user)).thenReturn(response);

            AuthRegisterResponse result = authService.register(request);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(response);

            verify(userRepository).existsByEmail(request.email());
            verify(passwordValidator).validate(request.password());
            verify(inputNormalizer).normalize(request.email());
            verify(inputNormalizer).normalize(request.firstName());
            verify(inputNormalizer).normalize(request.lastName());
            verify(passwordEncoder).encode(request.password());
            verify(userRepository).save(any(User.class));
            verify(authMapper).toAuthRegisterResponse(user);
        }

        @Test
        void shouldThrowEmailAlreadyExistsException_whenEmailIsAlreadyRegistered() {

            AuthRegisterRequest request = new AuthRegisterRequest(
                    "test@mail.com", "John", "Doe", "Password123"
            );

            when(userRepository.existsByEmail(request.email())).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(EmailAlreadyExistsException.class);

            verify(userRepository).existsByEmail(request.email());
            verify(passwordValidator, never()).validate(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        void shouldThrowInvalidPasswordFormatException_whenPasswordIsInvalid() {

            AuthRegisterRequest request = new AuthRegisterRequest(
                    "test@mail.com", "John", "Doe", "badpassword"
            );

            when(userRepository.existsByEmail(request.email())).thenReturn(false);
            doThrow(new InvalidPasswordFormatException())
                    .when(passwordValidator).validate(request.password());

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(InvalidPasswordFormatException.class);

            verify(passwordValidator).validate(request.password());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class LoginTests {

        @Test
        void shouldReturnTokens_whenValidCredentials() {

            AuthLoginRequest request = new AuthLoginRequest("test@mail.com", "Password123");
            User user = UserTestDataProvider.user(1L);
            CustomUserDetails userDetails = new CustomUserDetails(user);
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(accessTokenService.createAccessToken(user)).thenReturn("accessToken");
            when(refreshTokenService.createRefreshToken(user)).thenReturn("refreshToken");

            AuthTokenResponse result = authService.login(request);

            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo("accessToken");
            assertThat(result.refreshToken()).isEqualTo("refreshToken");

            verify(authenticationManager).authenticate(any());
            verify(accessTokenService).createAccessToken(user);
            verify(refreshTokenService).createRefreshToken(user);
        }

        @Test
        void shouldThrowException_whenCredentialsAreInvalid() {

            AuthLoginRequest request = new AuthLoginRequest("test@mail.com", "WrongPassword");

            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

            verify(accessTokenService, never()).createAccessToken(any());
            verify(refreshTokenService, never()).createRefreshToken(any());
        }
    }

    @Nested
    class RefreshTests {

        @Test
        void shouldReturnNewTokens_whenValidRefreshToken() {

            UUID rawToken = UUID.randomUUID();
            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(rawToken);
            User user = UserTestDataProvider.user(1L);
            RefreshToken refreshToken = mock(RefreshToken.class);

            when(refreshTokenService.validateRefreshToken(rawToken.toString()))
                    .thenReturn(refreshToken);
            when(refreshToken.getUser()).thenReturn(user);
            when(accessTokenService.createAccessToken(user)).thenReturn("newAccessToken");
            when(refreshTokenService.rotateRefreshToken(refreshToken)).thenReturn("newRefreshToken");

            AuthTokenResponse result = authService.refresh(request);

            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo("newAccessToken");
            assertThat(result.refreshToken()).isEqualTo("newRefreshToken");

            verify(refreshTokenService).validateRefreshToken(rawToken.toString());
            verify(accessTokenService).createAccessToken(user);
            verify(refreshTokenService).rotateRefreshToken(refreshToken);
        }

        @Test
        void shouldThrowRefreshTokenNotFoundException_whenTokenDoesNotExist() {

            UUID rawToken = UUID.randomUUID();
            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(rawToken);

            when(refreshTokenService.validateRefreshToken(rawToken.toString()))
                    .thenThrow(new RefreshTokenNotFoundException());

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(RefreshTokenNotFoundException.class);

            verify(accessTokenService, never()).createAccessToken(any());
            verify(refreshTokenService, never()).rotateRefreshToken(any());
        }

        @Test
        void shouldThrowRefreshTokenRevokedException_whenTokenIsRevoked() {

            UUID rawToken = UUID.randomUUID();
            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(rawToken);

            when(refreshTokenService.validateRefreshToken(rawToken.toString()))
                    .thenThrow(new RefreshTokenRevokedException());

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(RefreshTokenRevokedException.class);

            verify(accessTokenService, never()).createAccessToken(any());
            verify(refreshTokenService, never()).rotateRefreshToken(any());
        }

        @Test
        void shouldThrowRefreshTokenExpiredException_whenTokenIsExpired() {

            UUID rawToken = UUID.randomUUID();
            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(rawToken);

            when(refreshTokenService.validateRefreshToken(rawToken.toString()))
                    .thenThrow(new RefreshTokenExpiredException());

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(RefreshTokenExpiredException.class);

            verify(accessTokenService, never()).createAccessToken(any());
            verify(refreshTokenService, never()).rotateRefreshToken(any());
        }
    }

    @Nested
    class LogoutTests {

        @Test
        void shouldLogout_whenValidRefreshToken() {

            UUID rawToken = UUID.randomUUID();
            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(rawToken);

            doNothing().when(refreshTokenService).revokeRefreshToken(rawToken.toString());

            authService.logout(request);

            verify(refreshTokenService).revokeRefreshToken(rawToken.toString());
        }

        @Test
        void shouldThrowRefreshTokenNotFoundException_whenTokenDoesNotExist() {

            UUID rawToken = UUID.randomUUID();
            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(rawToken);

            doThrow(new RefreshTokenNotFoundException())
                    .when(refreshTokenService).revokeRefreshToken(rawToken.toString());

            assertThatThrownBy(() -> authService.logout(request))
                    .isInstanceOf(RefreshTokenNotFoundException.class);

            verify(refreshTokenService).revokeRefreshToken(rawToken.toString());
        }

        @Test
        void shouldThrowRefreshTokenRevokedException_whenTokenIsAlreadyRevoked() {

            UUID rawToken = UUID.randomUUID();
            AuthRefreshTokenRequest request = new AuthRefreshTokenRequest(rawToken);

            doThrow(new RefreshTokenRevokedException())
                    .when(refreshTokenService).revokeRefreshToken(rawToken.toString());

            assertThatThrownBy(() -> authService.logout(request))
                    .isInstanceOf(RefreshTokenRevokedException.class);

            verify(refreshTokenService).revokeRefreshToken(rawToken.toString());
        }
    }
}
