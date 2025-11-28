package com.example.auth.infrastructure;

import com.example.auth.domain.RefreshTokenStore;
import com.example.auth.domain.TokenPayload;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private final Map<String, TokenPayload> store = new ConcurrentHashMap<>();

    @Override
    public void store(String token, TokenPayload payload) {
        store.put(token, payload);
    }

    @Override
    public Optional<TokenPayload> find(String token) {
        return Optional.ofNullable(store.get(token))
                .filter(p -> !p.isExpired(Instant.now()));
    }

    @Override
    public void remove(String token) {
        store.remove(token);
    }
}
