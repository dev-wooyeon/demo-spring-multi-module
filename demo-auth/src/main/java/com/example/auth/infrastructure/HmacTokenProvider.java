package com.example.auth.infrastructure;

import com.example.auth.application.AuthProperties;
import com.example.auth.domain.AuthErrorCode;
import com.example.auth.domain.TokenPayload;
import com.example.auth.domain.TokenProvider;
import com.example.auth.domain.TokenType;
import com.example.core.domain.DomainException;
import com.example.core.time.TimeProvider;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class HmacTokenProvider implements TokenProvider {

    private static final String ALGORITHM = "HmacSHA256";

    private final AuthProperties properties;
    private final TimeProvider timeProvider;

    public HmacTokenProvider(AuthProperties properties, TimeProvider timeProvider) {
        this.properties = properties;
        this.timeProvider = timeProvider;
    }

    @Override
    public String create(TokenPayload payload) {
        String encodedPayload = encodePayload(payload);
        String signature = sign(encodedPayload);
        return encodedPayload + "." + signature;
    }

    @Override
    public TokenPayload verify(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new DomainException(AuthErrorCode.INVALID_TOKEN);
        }
        String payloadPart = parts[0];
        String signature = parts[1];
        if (!sign(payloadPart).equals(signature)) {
            throw new DomainException(AuthErrorCode.INVALID_TOKEN);
        }
        TokenPayload payload = decodePayload(payloadPart);
        if (payload.isExpired(timeProvider.now())) {
            throw new DomainException(AuthErrorCode.TOKEN_EXPIRED);
        }
        return payload;
    }

    private String encodePayload(TokenPayload payload) {
        String raw = String.join("|",
                payload.type().name(),
                String.valueOf(payload.memberId()),
                payload.email(),
                String.join(",", payload.roles()),
                String.valueOf(payload.issuedAt().getEpochSecond()),
                String.valueOf(payload.expiresAt().getEpochSecond()));
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private TokenPayload decodePayload(String encoded) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|");
            if (parts.length != 6) {
                throw new DomainException(AuthErrorCode.INVALID_TOKEN);
            }
            TokenType type = TokenType.valueOf(parts[0]);
            Long memberId = Long.valueOf(parts[1]);
            String email = parts[2];
            Set<String> roles = Set.of(parts[3].split(","));
            Instant issuedAt = Instant.ofEpochSecond(Long.parseLong(parts[4]));
            Instant expiresAt = Instant.ofEpochSecond(Long.parseLong(parts[5]));
            return new TokenPayload(memberId, email, roles, issuedAt, expiresAt, type);
        } catch (IllegalArgumentException e) {
            throw new DomainException(AuthErrorCode.INVALID_TOKEN, e);
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(properties.getHmacSecret().getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC signing failed", e);
        }
    }
}
