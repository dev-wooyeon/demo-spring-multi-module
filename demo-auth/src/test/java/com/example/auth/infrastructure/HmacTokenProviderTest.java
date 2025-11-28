package com.example.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.auth.application.AuthProperties;
import com.example.auth.domain.AuthErrorCode;
import com.example.auth.domain.TokenPayload;
import com.example.auth.domain.TokenType;
import com.example.core.domain.DomainException;
import com.example.core.time.TimeProvider;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HmacTokenProviderTest {

    private static final Instant FIXED_NOW = Instant.parse("2024-01-01T00:00:00Z");
    private final AuthProperties properties = new AuthProperties();
    private final TimeProvider timeProvider = () -> FIXED_NOW;
    private final HmacTokenProvider provider = new HmacTokenProvider(properties, timeProvider);

    @Test
    void create_and_verify_round_trip() {
        TokenPayload payload = new TokenPayload(
                1L,
                "round@trip.com",
                Set.of("USER"),
                FIXED_NOW,
                FIXED_NOW.plusSeconds(60),
                TokenType.ACCESS
        );

        String token = provider.create(payload);
        TokenPayload verified = provider.verify(token);

        assertThat(verified.memberId()).isEqualTo(1L);
        assertThat(verified.email()).isEqualTo("round@trip.com");
        assertThat(verified.roles()).containsExactly("USER");
        assertThat(verified.type()).isEqualTo(TokenType.ACCESS);
    }

    @Test
    void verify_fails_when_expired() {
        TokenPayload payload = new TokenPayload(
                1L,
                "expired@demo.com",
                Set.of("USER"),
                FIXED_NOW.minusSeconds(120),
                FIXED_NOW.minusSeconds(60),
                TokenType.ACCESS
        );

        String token = provider.create(payload);

        assertThatThrownBy(() -> provider.verify(token))
                .isInstanceOf(DomainException.class)
                .matches(ex -> ((DomainException) ex).errorCode() == AuthErrorCode.TOKEN_EXPIRED);
    }
}
