package com.example.member.domain;

import com.example.core.domain.BaseEntity;
import com.example.core.time.TimeProvider;
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

    private Instant lastLoginAt;

    private Member(Email email, String name, String passwordHash, TimeProvider timeProvider) {
        this.email = Objects.requireNonNull(email, "email");
        this.name = Objects.requireNonNull(name, "name");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.status = MemberStatus.ACTIVE;
        this.roles.add(MemberRole.USER);
        this.lastLoginAt = timeProvider.now();
    }

    public static Member register(Email email, String name, String passwordHash, TimeProvider timeProvider) {
        return new Member(email, name, passwordHash, timeProvider);
    }

    public void touchLogin(TimeProvider timeProvider) {
        this.lastLoginAt = timeProvider.now();
    }

    public Set<MemberRole> getRoles() {
        return Set.copyOf(roles);
    }

    public void lock() {
        this.status = MemberStatus.LOCKED;
    }
}
