package com.example.member.application;

import com.example.core.domain.DomainException;
import com.example.core.time.TimeProvider;
import com.example.member.domain.Email;
import com.example.member.domain.Member;
import com.example.member.domain.MemberErrorCode;
import com.example.member.domain.MemberRepository;
import com.example.member.domain.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberCommandService {

    private final MemberRepository memberRepository;
    private final PasswordHasher passwordHasher;
    private final TimeProvider timeProvider;

    public MemberCommandService(
            MemberRepository memberRepository,
            PasswordHasher passwordHasher,
            TimeProvider timeProvider
    ) {
        this.memberRepository = memberRepository;
        this.passwordHasher = passwordHasher;
        this.timeProvider = timeProvider;
    }

    @Transactional
    public Member register(MemberRegisterCommand command) {
        Email email = new Email(command.email());
        if (memberRepository.existsByEmail(email)) {
            throw new DomainException(MemberErrorCode.EMAIL_DUPLICATED);
        }
        String hashedPassword = passwordHasher.hash(command.rawPassword());
        Member member = Member.register(email, command.name(), hashedPassword, timeProvider);
        return memberRepository.save(member);
    }
}
