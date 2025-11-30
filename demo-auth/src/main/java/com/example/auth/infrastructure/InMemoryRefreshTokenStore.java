package com.example.auth.infrastructure;

import com.example.auth.domain.RefreshTokenStore;
import com.example.core.time.TimeProvider;
import com.example.auth.domain.TokenPayload;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@Primary
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private final Map<String, TokenPayload> store = new ConcurrentHashMap<>();
    private final TimeProvider timeProvider;

    public InMemoryRefreshTokenStore(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public void store(String token, TokenPayload payload) {
        store.put(token, payload);
    }

    @Override
    public Optional<TokenPayload> find(String token) {
        return Optional.ofNullable(store.get(token))
                .filter(p -> !p.isExpired(timeProvider.now()));
    }

    @Override
    public void remove(String token) {
        store.remove(token);
    }
}
