package com.example.api.api;

import com.example.auth.application.AuthService;
import com.example.auth.application.LoginCommand;
import com.example.auth.application.RefreshCommand;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                TokenResponse.from(
                        authService.login(new LoginCommand(request.email(), request.password()))
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(
                TokenResponse.from(
                        authService.refresh(new RefreshCommand(request.refreshToken()))
                )
        );
    }
}
