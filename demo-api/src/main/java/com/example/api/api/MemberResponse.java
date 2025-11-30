package com.example.api.api;

import com.example.member.application.MemberSummary;
import com.example.member.domain.MemberRole;
import com.example.member.domain.MemberStatus;
import java.time.Instant;
import java.util.Set;

public record MemberResponse(
        Long id,
        String email,
        String name,
        MemberStatus status,
        Set<MemberRole> roles,
        Instant lastLoginAt
) {
    public static MemberResponse from(MemberSummary summary) {
        return new MemberResponse(
                summary.id(),
                summary.email(),
                summary.name(),
                summary.status(),
                summary.roles(),
                summary.lastLoginAt()
        );
    }

    public static MemberResponse from(com.example.member.domain.Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail().getValue(),
                member.getName(),
                member.getStatus(),
                member.getRoles(),
                member.getLastLoginAt()
        );
    }
}
