package com.example.api.api;

import com.example.auth.domain.AuthTokens;
import java.time.Instant;

public record TokenResponse(String accessToken, String refreshToken, Instant accessTokenExpiresAt) {
    public static TokenResponse from(AuthTokens tokens) {
        return new TokenResponse(tokens.accessToken(), tokens.refreshToken(), tokens.accessTokenExpiresAt());
    }
}
