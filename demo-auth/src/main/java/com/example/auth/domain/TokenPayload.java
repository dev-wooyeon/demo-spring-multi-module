package com.example.auth.domain;

import java.time.Instant;
import java.util.Set;

public record TokenPayload(
        Long memberId,
        String email,
        Set<String> roles,
        Instant issuedAt,
        Instant expiresAt,
        TokenType type
) {
    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt) || now.equals(expiresAt);
    }
}
