package com.example.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

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
import com.example.member.domain.PasswordHasher;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2024-01-01T00:00:00Z");

    @Mock
    MemberRepository memberRepository;

    @Mock
    PasswordHasher passwordHasher;

    @Mock
    TokenProvider tokenProvider;

    @Mock
    RefreshTokenStore refreshTokenStore;

    private AuthProperties properties;
    private TimeProvider timeProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        properties = new AuthProperties();
        properties.setAccessTtl(Duration.ofMinutes(10));
        properties.setRefreshTtl(Duration.ofHours(2));
        timeProvider = () -> FIXED_NOW;
        authService = new AuthService(
                memberRepository,
                passwordHasher,
                tokenProvider,
                refreshTokenStore,
                properties,
                timeProvider
        );
    }

    @Test
    @DisplayName("로그인 성공 시 액세스/리프레시 토큰을 발급하고 마지막 로그인 시각을 갱신한다")
    void loginIssuesTokensAndStoresRefresh() {
        Member member = Member.register(
                new Email("login@demo.com"),
                "Login User",
                "hashed",
                "code-1",
                FIXED_NOW.plusSeconds(3600));
        member.activate("code-1", timeProvider);
        given(memberRepository.findByEmail(new Email("login@demo.com"))).willReturn(Optional.of(member));
        given(passwordHasher.matches("raw-pass", "hashed")).willReturn(true);
        given(tokenProvider.create(any(TokenPayload.class))).willReturn("access-token", "refresh-token");

        AuthTokens tokens = authService.login(new LoginCommand("login@demo.com", "raw-pass"));

        ArgumentCaptor<TokenPayload> payloadCaptor = ArgumentCaptor.forClass(TokenPayload.class);
        then(tokenProvider).should(times(2)).create(payloadCaptor.capture());
        List<TokenPayload> createdPayloads = payloadCaptor.getAllValues();

        assertThat(tokens.accessToken()).isEqualTo("access-token");
        assertThat(tokens.refreshToken()).isEqualTo("refresh-token");
        assertThat(tokens.accessTokenExpiresAt()).isEqualTo(FIXED_NOW.plus(properties.getAccessTtl()));
        assertThat(createdPayloads.get(0).type()).isEqualTo(TokenType.ACCESS);
        assertThat(createdPayloads.get(0).expiresAt()).isEqualTo(FIXED_NOW.plus(properties.getAccessTtl()));
        assertThat(createdPayloads.get(1).type()).isEqualTo(TokenType.REFRESH);
        assertThat(createdPayloads.get(1).expiresAt()).isEqualTo(FIXED_NOW.plus(properties.getRefreshTtl()));
        assertThat(member.getLastLoginAt()).isEqualTo(FIXED_NOW);

        then(refreshTokenStore).should().store(eq("refresh-token"), eq(createdPayloads.get(1)));
        then(memberRepository).should().save(member);
    }

    @Test
    @DisplayName("잠긴 회원은 로그인 시도를 차단한다")
    void loginFailsWhenMemberLocked() {
        Member member = Member.register(
                new Email("lock@demo.com"),
                "Locked User",
                "hashed",
                "code-2",
                FIXED_NOW.plusSeconds(3600));
        member.activate("code-2", timeProvider);
        member.lock();
        given(memberRepository.findByEmail(new Email("lock@demo.com"))).willReturn(Optional.of(member));

        assertThatThrownBy(() -> authService.login(new LoginCommand("lock@demo.com", "secret"))).isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).errorCode())
                .isEqualTo(MemberErrorCode.MEMBER_LOCKED);

        then(tokenProvider).shouldHaveNoInteractions();
        then(refreshTokenStore).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("패스워드가 틀리면 로그인에 실패한다")
    void loginFailsWhenPasswordMismatch() {
        Member member = Member.register(
                new Email("pw@demo.com"),
                "Password User",
                "hashed",
                "code-3",
                FIXED_NOW.plusSeconds(3600));
        member.activate("code-3", timeProvider);
        given(memberRepository.findByEmail(new Email("pw@demo.com"))).willReturn(Optional.of(member));
        given(passwordHasher.matches("wrong", "hashed")).willReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginCommand("pw@demo.com", "wrong")))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).errorCode())
                .isEqualTo(AuthErrorCode.INVALID_CREDENTIAL);

        then(tokenProvider).shouldHaveNoInteractions();
        then(refreshTokenStore).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("리프레시 토큰 재발급 시 새 토큰을 저장하고 기존 토큰을 제거한다")
    void refreshReissuesTokensAndReplacesStoredToken() {
        TokenPayload storedPayload = new TokenPayload(
                10L,
                "refresh@demo.com",
                Set.of("USER"),
                FIXED_NOW.minusSeconds(30),
                FIXED_NOW.plusSeconds(1000),
                TokenType.REFRESH
        );
        Member member = Member.register(
                new Email("refresh@demo.com"),
                "Refresher",
                "hashed",
                "code-4",
                FIXED_NOW.plusSeconds(3600));
        member.activate("code-4", timeProvider);
        given(tokenProvider.verify("old-refresh")).willReturn(storedPayload);
        given(refreshTokenStore.find("old-refresh")).willReturn(Optional.of(storedPayload));
        given(memberRepository.findById(10L)).willReturn(Optional.of(member));
        given(tokenProvider.create(any(TokenPayload.class))).willReturn("new-access", "new-refresh");

        AuthTokens tokens = authService.refresh(new RefreshCommand("old-refresh"));

        ArgumentCaptor<TokenPayload> payloadCaptor = ArgumentCaptor.forClass(TokenPayload.class);
        then(tokenProvider).should(times(2)).create(payloadCaptor.capture());
        TokenPayload newRefreshPayload = payloadCaptor.getAllValues().get(1);

        assertThat(tokens.accessToken()).isEqualTo("new-access");
        assertThat(tokens.refreshToken()).isEqualTo("new-refresh");
        assertThat(tokens.accessTokenExpiresAt()).isEqualTo(FIXED_NOW.plus(properties.getAccessTtl()));
        assertThat(newRefreshPayload.type()).isEqualTo(TokenType.REFRESH);

        then(refreshTokenStore).should().store("new-refresh", newRefreshPayload);
        then(refreshTokenStore).should().remove("old-refresh");
    }

    @Test
    @DisplayName("리프레시 토큰이 아니면 재발급을 거절한다")
    void refreshFailsWhenTokenTypeIsNotRefresh() {
        TokenPayload accessPayload = new TokenPayload(
                1L,
                "access@demo.com",
                Set.of("USER"),
                FIXED_NOW.minusSeconds(30),
                FIXED_NOW.plusSeconds(1000),
                TokenType.ACCESS
        );
        given(tokenProvider.verify("access-token")).willReturn(accessPayload);

        assertThatThrownBy(() -> authService.refresh(new RefreshCommand("access-token")))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).errorCode())
                .isEqualTo(AuthErrorCode.INVALID_TOKEN);

        then(refreshTokenStore).should(never()).store(any(), any());
        then(refreshTokenStore).should(never()).remove(any());
    }

    @Test
    @DisplayName("리프레시 토큰 저장소에서 찾을 수 없으면 재발급을 거절한다")
    void refreshFailsWhenStoreDoesNotHaveToken() {
        TokenPayload refreshPayload = new TokenPayload(
                5L,
                "missing@demo.com",
                Set.of("USER"),
                FIXED_NOW.minusSeconds(30),
                FIXED_NOW.plusSeconds(1000),
                TokenType.REFRESH
        );
        given(tokenProvider.verify("missing-refresh")).willReturn(refreshPayload);
        given(refreshTokenStore.find("missing-refresh")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(new RefreshCommand("missing-refresh")))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).errorCode())
                .isEqualTo(AuthErrorCode.REFRESH_NOT_FOUND);

        then(tokenProvider).should(never()).create(any(TokenPayload.class));
    }

    @Test
    @DisplayName("비활성 회원의 리프레시 토큰 재발급을 거절한다")
    void refreshFailsWhenMemberInactive() {
        TokenPayload refreshPayload = new TokenPayload(
                7L,
                "pending@demo.com",
                Set.of("USER"),
                FIXED_NOW.minusSeconds(30),
                FIXED_NOW.plusSeconds(1000),
                TokenType.REFRESH
        );
        Member member = Member.register(
                new Email("pending@demo.com"),
                "Pending",
                "hashed",
                "code-5",
                FIXED_NOW.plusSeconds(3600));
        given(tokenProvider.verify("pending-refresh")).willReturn(refreshPayload);
        given(refreshTokenStore.find("pending-refresh")).willReturn(Optional.of(refreshPayload));
        given(memberRepository.findById(7L)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> authService.refresh(new RefreshCommand("pending-refresh")))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).errorCode())
                .isEqualTo(MemberErrorCode.MEMBER_INACTIVE);

        then(tokenProvider).should(never()).create(any(TokenPayload.class));
        then(refreshTokenStore).should(never()).store(any(), any());
    }
}
