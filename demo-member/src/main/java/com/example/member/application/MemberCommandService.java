package com.example.member.application;

import com.example.core.domain.DomainException;
import com.example.core.time.TimeProvider;
import com.example.member.domain.Email;
import com.example.member.domain.Member;
import com.example.member.domain.MemberErrorCode;
import com.example.member.domain.MemberRepository;
import com.example.member.domain.PasswordHasher;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberCommandService {

    private static final Duration ACTIVATION_TTL = Duration.ofHours(24);

    private final MemberRepository memberRepository;
    private final PasswordHasher passwordHasher;
    private final TimeProvider timeProvider;
    private final ActivationNotifier activationNotifier;

    public MemberCommandService(
            MemberRepository memberRepository,
            PasswordHasher passwordHasher,
            TimeProvider timeProvider,
            ActivationNotifier activationNotifier
    ) {
        this.memberRepository = memberRepository;
        this.passwordHasher = passwordHasher;
        this.timeProvider = timeProvider;
        this.activationNotifier = activationNotifier;
    }

    @Transactional
    public Member register(MemberRegisterCommand command) {
        Email email = new Email(command.email());
        if (memberRepository.existsByEmail(email)) {
            throw new DomainException(MemberErrorCode.EMAIL_DUPLICATED);
        }
        String hashedPassword = passwordHasher.hash(command.rawPassword());
        Instant now = timeProvider.now();
        String activationCode = UUID.randomUUID().toString();
        Instant activationExpiresAt = now.plus(ACTIVATION_TTL);
        Member member = Member.register(email, command.name(), hashedPassword, activationCode, activationExpiresAt);
        Member saved = memberRepository.save(member);
        activationNotifier.notify(email, activationCode, activationExpiresAt);
        return saved;
    }
}
