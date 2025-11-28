package com.example.member.infrastructure;

import com.example.member.domain.Email;
import com.example.member.domain.Member;
import com.example.member.domain.MemberRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepositoryAdapter implements MemberRepository {

    private final JpaMemberRepository repository;

    public MemberRepositoryAdapter(JpaMemberRepository repository) {
        this.repository = repository;
    }

    @Override
    public Member save(Member member) {
        return repository.save(member);
    }

    @Override
    public Optional<Member> findByEmail(Email email) {
        return repository.findByEmail(email);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return repository.existsByEmail(email);
    }
}
