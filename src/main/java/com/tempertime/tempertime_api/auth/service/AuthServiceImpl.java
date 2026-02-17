package com.tempertime.tempertime_api.auth.service;

import com.tempertime.tempertime_api.auth.dto.request.AuthLoginRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRefreshTokenRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRegisterRequest;
import com.tempertime.tempertime_api.auth.dto.response.AuthRegisterResponse;
import com.tempertime.tempertime_api.auth.dto.response.AuthTokenResponse;
import com.tempertime.tempertime_api.auth.exception.EmailAlreadyExistsException;
import com.tempertime.tempertime_api.auth.mapper.AuthMapper;
import com.tempertime.tempertime_api.common.validator.PasswordValidator;
import com.tempertime.tempertime_api.security.core.CustomUserDetails;
import com.tempertime.tempertime_api.security.jwt.AccessTokenService;
import com.tempertime.tempertime_api.security.refresh.RefreshToken;
import com.tempertime.tempertime_api.security.refresh.RefreshTokenService;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // Repositories
    private final UserRepository userRepository;

    // Security / Authentication
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // Services
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    // Mappers
    private final AuthMapper authMapper;

    // Validators
    private final PasswordValidator passwordValidator;

    @Override
    public AuthRegisterResponse register(AuthRegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        passwordValidator.validate(request.password());

        User user = User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        return authMapper.toAuthRegisterResponse(userRepository.save(user));
    }

    @Override
    public AuthTokenResponse login(AuthLoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // Principal exposes domain User through CustomUserDetails wrapper
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();

        String accessToken = accessTokenService.createAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthTokenResponse(accessToken, refreshToken);
    }

    /**
     * Performs refresh token rotation to prevent token reuse attacks.
     */
    @Override
    public AuthTokenResponse refresh(AuthRefreshTokenRequest request) {

        // Validates refresh token and loads associated user
        RefreshToken refreshToken =
                refreshTokenService.validateRefreshToken(request.refreshToken().toString());
        User user = refreshToken.getUser();

        String newAccessToken = accessTokenService.createAccessToken(user);

        // Rotates refresh token to prevent reuse
        String newRefreshToken =
                refreshTokenService.rotateRefreshToken(refreshToken);

        return new AuthTokenResponse(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(AuthRefreshTokenRequest request) {

        refreshTokenService.revokeRefreshToken(request.refreshToken().toString());
    }
}
