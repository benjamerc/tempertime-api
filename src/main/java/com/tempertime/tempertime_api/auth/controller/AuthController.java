package com.tempertime.tempertime_api.auth.controller;

import com.tempertime.tempertime_api.auth.controller.docs.AuthControllerDocs;
import com.tempertime.tempertime_api.auth.dto.request.AuthLoginRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRefreshTokenRequest;
import com.tempertime.tempertime_api.auth.dto.request.AuthRegisterRequest;
import com.tempertime.tempertime_api.auth.dto.response.AuthRegisterResponse;
import com.tempertime.tempertime_api.auth.dto.response.AuthTokenResponse;
import com.tempertime.tempertime_api.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    @Override
    @PostMapping("/register")
    public ResponseEntity<AuthRegisterResponse> register(@RequestBody @Valid AuthRegisterRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@RequestBody @Valid AuthLoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(@RequestBody @Valid AuthRefreshTokenRequest request) {

        return ResponseEntity.ok(authService.refresh(request));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid AuthRefreshTokenRequest request) {

        authService.logout(request);

        return ResponseEntity.noContent().build();
    }
}
