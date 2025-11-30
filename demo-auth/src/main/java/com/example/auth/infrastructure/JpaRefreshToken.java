package com.example.auth.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "refresh_tokens")
class JpaRefreshToken {

    @Id
    @Column(length = 255)
    private String token;

    private Long memberId;

    private String email;

    @Column(length = 200)
    private String roles;

    private Instant issuedAt;

    private Instant expiresAt;

    private JpaRefreshToken(String token, Long memberId, String email, String roles, Instant issuedAt, Instant expiresAt) {
        this.token = token;
        this.memberId = memberId;
        this.email = email;
        this.roles = roles;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    static JpaRefreshToken from(String token, com.example.auth.domain.TokenPayload payload) {
        String joinedRoles = String.join(",", payload.roles());
        return new JpaRefreshToken(token, payload.memberId(), payload.email(), joinedRoles, payload.issuedAt(), payload.expiresAt());
    }
}
