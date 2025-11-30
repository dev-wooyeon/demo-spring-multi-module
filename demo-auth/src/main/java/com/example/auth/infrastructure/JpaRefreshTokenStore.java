package com.example.auth.infrastructure;

import com.example.auth.domain.RefreshTokenStore;
import com.example.auth.domain.TokenPayload;
import com.example.core.time.TimeProvider;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class JpaRefreshTokenStore implements RefreshTokenStore {

    private final JpaRefreshTokenRepository repository;
    private final TimeProvider timeProvider;

    @Override
    @Transactional
    public void store(String token, TokenPayload payload) {
        repository.save(JpaRefreshToken.from(token, payload));
        repository.deleteExpired(timeProvider.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TokenPayload> find(String token) {
        return repository.findByTokenAndExpiresAtAfter(token, timeProvider.now())
                .map(this::toPayload);
    }

    @Override
    @Transactional
    public void remove(String token) {
        repository.deleteById(token);
    }

    private TokenPayload toPayload(JpaRefreshToken entity) {
        Set<String> roles = entity.getRoles().isBlank()
                ? Set.of()
                : Set.of(entity.getRoles().split(","));
        return new TokenPayload(
                entity.getMemberId(),
                entity.getEmail(),
                roles,
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                com.example.auth.domain.TokenType.REFRESH
        );
    }
}
