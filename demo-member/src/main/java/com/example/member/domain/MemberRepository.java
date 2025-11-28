package com.example.member.domain;

import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);

    Optional<Member> findByEmail(Email email);

    Optional<Member> findById(Long id);

    boolean existsByEmail(Email email);
}
