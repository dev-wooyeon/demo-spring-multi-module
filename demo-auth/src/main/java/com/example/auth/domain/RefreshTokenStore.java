package com.example.auth.domain;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenStore {
    void store(String token, TokenPayload payload);

    Optional<TokenPayload> find(String token);

    void remove(String token);

    default boolean isValid(String token, Instant now) {
        return find(token).filter(p -> !p.isExpired(now)).isPresent();
    }
}
