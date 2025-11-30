package com.example.member.application;

import com.example.core.domain.DomainException;
import com.example.core.time.TimeProvider;
import com.example.member.domain.Email;
import com.example.member.domain.Member;
import com.example.member.domain.MemberErrorCode;
import com.example.member.domain.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberActivationService {

    private final MemberRepository memberRepository;
    private final TimeProvider timeProvider;

    public MemberActivationService(MemberRepository memberRepository, TimeProvider timeProvider) {
        this.memberRepository = memberRepository;
        this.timeProvider = timeProvider;
    }

    @Transactional
    public Member activate(MemberActivationCommand command) {
        Member member = memberRepository.findByEmail(new Email(command.email()))
                .orElseThrow(() -> new DomainException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.activate(command.code(), timeProvider);
        return memberRepository.save(member);
    }
}
