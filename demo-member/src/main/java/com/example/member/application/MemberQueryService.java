package com.example.member.application;

import com.example.core.domain.DomainException;
import com.example.member.domain.Email;
import com.example.member.domain.Member;
import com.example.member.domain.MemberErrorCode;
import com.example.member.domain.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberQueryService {

    private final MemberRepository memberRepository;

    public MemberQueryService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public MemberSummary getById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new DomainException(MemberErrorCode.MEMBER_NOT_FOUND));
        return toSummary(member);
    }

    @Transactional(readOnly = true)
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new DomainException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private MemberSummary toSummary(Member member) {
        return new MemberSummary(
                member.getId(),
                member.getEmail().getValue(),
                member.getName(),
                member.getStatus(),
                member.getRoles(),
                member.getLastLoginAt()
        );
    }
}
