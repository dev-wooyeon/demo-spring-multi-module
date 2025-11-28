package com.example.member.infrastructure;

import com.example.member.domain.Email;
import com.example.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaMemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(Email email);

    boolean existsByEmail(Email email);
}
