package com.example.auth.application;

import com.example.auth.domain.AuthErrorCode;
import com.example.auth.domain.AuthTokens;
import com.example.auth.domain.RefreshTokenStore;
import com.example.auth.domain.TokenPayload;
import com.example.auth.domain.TokenProvider;
import com.example.auth.domain.TokenType;
import com.example.core.domain.DomainException;
import com.example.core.time.TimeProvider;
import com.example.member.domain.Email;
import com.example.member.domain.Member;
import com.example.member.domain.MemberErrorCode;
import com.example.member.domain.MemberRepository;
import com.example.member.domain.MemberStatus;
import com.example.member.domain.PasswordHasher;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final AuthProperties properties;
    private final TimeProvider timeProvider;

    public AuthService(
            MemberRepository memberRepository,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider,
            RefreshTokenStore refreshTokenStore,
            AuthProperties properties,
            TimeProvider timeProvider
    ) {
        this.memberRepository = memberRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.refreshTokenStore = refreshTokenStore;
        this.properties = properties;
        this.timeProvider = timeProvider;
    }

    @Transactional
    public AuthTokens login(LoginCommand command) {
        Member member = memberRepository.findByEmail(new Email(command.email()))
                .orElseThrow(() -> new DomainException(MemberErrorCode.MEMBER_NOT_FOUND));

        ensureActive(member);

        if (!passwordHasher.matches(command.rawPassword(), member.getPasswordHash())) {
            throw new DomainException(AuthErrorCode.INVALID_CREDENTIAL);
        }

        Instant now = timeProvider.now();
        TokenPayload accessPayload = buildPayload(member, TokenType.ACCESS, now, properties.getAccessTtl().toSeconds());
        TokenPayload refreshPayload = buildPayload(member, TokenType.REFRESH, now, properties.getRefreshTtl().toSeconds());

        String accessToken = tokenProvider.create(accessPayload);
        String refreshToken = tokenProvider.create(refreshPayload);

        refreshTokenStore.store(refreshToken, refreshPayload);
        member.touchLogin(timeProvider);
        memberRepository.save(member);

        return new AuthTokens(accessToken, refreshToken, accessPayload.expiresAt());
    }

    @Transactional
    public AuthTokens refresh(RefreshCommand command) {
        TokenPayload payload = tokenProvider.verify(command.refreshToken());
        if (payload.type() != TokenType.REFRESH) {
            throw new DomainException(AuthErrorCode.INVALID_TOKEN);
        }
        TokenPayload stored = refreshTokenStore.find(command.refreshToken())
                .orElseThrow(() -> new DomainException(AuthErrorCode.REFRESH_NOT_FOUND));

        Member member = memberRepository.findById(stored.memberId())
                .orElseThrow(() -> new DomainException(MemberErrorCode.MEMBER_NOT_FOUND));

        ensureActive(member);

        Instant now = timeProvider.now();
        TokenPayload newAccessPayload = buildPayload(member, TokenType.ACCESS, now, properties.getAccessTtl().toSeconds());
        TokenPayload newRefreshPayload = buildPayload(member, TokenType.REFRESH, now, properties.getRefreshTtl().toSeconds());

        String accessToken = tokenProvider.create(newAccessPayload);
        String refreshToken = tokenProvider.create(newRefreshPayload);

        refreshTokenStore.store(refreshToken, newRefreshPayload);
        refreshTokenStore.remove(command.refreshToken());

        return new AuthTokens(accessToken, refreshToken, newAccessPayload.expiresAt());
    }

    private void ensureActive(Member member) {
        if (member.getStatus() == MemberStatus.LOCKED) {
            throw new DomainException(MemberErrorCode.MEMBER_LOCKED);
        }
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new DomainException(MemberErrorCode.MEMBER_INACTIVE);
        }
    }

    private TokenPayload buildPayload(Member member, TokenType type, Instant issuedAt, long ttlSeconds) {
        Instant expiresAt = issuedAt.plusSeconds(ttlSeconds);
        Set<String> roles = member.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());

        return new TokenPayload(
                member.getId(),
                member.getEmail().getValue(),
                roles,
                issuedAt,
                expiresAt,
                type
        );
    }
}
