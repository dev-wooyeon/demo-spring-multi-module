package com.example.member.domain;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.DomainException;
import com.example.core.time.TimeProvider;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.util.StringUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "members")
public class Member extends BaseEntity {

    @Embedded
    private Email email;

    private String name;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<MemberRole> roles = new LinkedHashSet<>();

    @Column
    private Instant lastLoginAt;

    @Column(length = 100)
    private String activationCode;

    @Column
    private Instant activationExpiresAt;

    private Member(
            Email email,
            String name,
            String passwordHash,
            String activationCode,
            Instant activationExpiresAt
    ) {
        this.email = Objects.requireNonNull(email, "email");
        this.name = Objects.requireNonNull(name, "name");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.status = MemberStatus.PENDING;
        this.roles.add(MemberRole.USER);
        this.activationCode = Objects.requireNonNull(activationCode, "activationCode");
        this.activationExpiresAt = Objects.requireNonNull(activationExpiresAt, "activationExpiresAt");
        this.lastLoginAt = null;
    }

    public static Member register(
            Email email,
            String name,
            String passwordHash,
            String activationCode,
            Instant activationExpiresAt
    ) {
        if (!StringUtils.hasText(activationCode)) {
            throw new IllegalArgumentException("Activation code is required");
        }
        return new Member(email, name, passwordHash, activationCode, activationExpiresAt);
    }

    public void touchLogin(TimeProvider timeProvider) {
        this.lastLoginAt = timeProvider.now();
    }

    public Set<MemberRole> getRoles() {
        return Set.copyOf(roles);
    }

    public void activate() {
        this.status = MemberStatus.ACTIVE;
    }

    public void activate(String code, TimeProvider timeProvider) {
        if (this.status == MemberStatus.ACTIVE) {
            throw new DomainException(MemberErrorCode.MEMBER_ALREADY_ACTIVE);
        }
        if (!Objects.equals(this.activationCode, code)) {
            throw new DomainException(MemberErrorCode.ACTIVATION_INVALID);
        }
        if (activationExpiresAt != null && !activationExpiresAt.isAfter(timeProvider.now())) {
            throw new DomainException(MemberErrorCode.ACTIVATION_EXPIRED);
        }
        this.status = MemberStatus.ACTIVE;
        this.activationCode = null;
        this.activationExpiresAt = null;
    }

    public void markPending() {
        this.status = MemberStatus.PENDING;
    }

    public void lock() {
        this.status = MemberStatus.LOCKED;
    }
}
