package com.example.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.auth.domain.TokenPayload;
import com.example.auth.domain.TokenType;
import com.example.core.time.TimeProvider;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryRefreshTokenStoreTest {

    private static final Instant FIXED_NOW = Instant.parse("2024-02-01T00:00:00Z");

    @Test
    @DisplayName("리프레시 토큰이 만료되면 저장소에서 조회되지 않는다")
    void findReturnsEmptyAfterExpiry() {
        MutableTimeProvider timeProvider = new MutableTimeProvider(FIXED_NOW);
        InMemoryRefreshTokenStore store = new InMemoryRefreshTokenStore(timeProvider);
        TokenPayload payload = new TokenPayload(
                1L,
                "store@demo.com",
                Set.of("USER"),
                FIXED_NOW,
                FIXED_NOW.plusSeconds(60),
                TokenType.REFRESH
        );

        store.store("token", payload);
        assertThat(store.find("token")).contains(payload);

        timeProvider.advanceSeconds(120);
        assertThat(store.find("token")).isEmpty();
    }

    @Test
    @DisplayName("토큰을 제거하면 더 이상 조회되지 않는다")
    void removeDeletesToken() {
        MutableTimeProvider timeProvider = new MutableTimeProvider(FIXED_NOW);
        InMemoryRefreshTokenStore store = new InMemoryRefreshTokenStore(timeProvider);
        TokenPayload payload = new TokenPayload(
                2L,
                "remove@demo.com",
                Set.of("USER"),
                FIXED_NOW,
                FIXED_NOW.plusSeconds(300),
                TokenType.REFRESH
        );

        store.store("t-1", payload);
        assertThat(store.find("t-1")).isPresent();

        store.remove("t-1");
        assertThat(store.find("t-1")).isEmpty();
    }

    private static class MutableTimeProvider implements TimeProvider {
        private Instant now;

        private MutableTimeProvider(Instant now) {
            this.now = now;
        }

        @Override
        public Instant now() {
            return now;
        }

        void advanceSeconds(long seconds) {
            now = now.plusSeconds(seconds);
        }
    }
}
