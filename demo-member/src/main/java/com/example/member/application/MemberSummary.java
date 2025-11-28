package com.example.member.application;

import com.example.member.domain.MemberRole;
import com.example.member.domain.MemberStatus;
import java.time.Instant;
import java.util.Set;

public record MemberSummary(
        Long id,
        String email,
        String name,
        MemberStatus status,
        Set<MemberRole> roles,
        Instant lastLoginAt
) {
}
