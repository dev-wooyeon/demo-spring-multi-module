package com.example.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.core.domain.DomainException;
import com.example.core.time.TimeProvider;
import com.example.member.domain.Email;
import com.example.member.domain.Member;
import com.example.member.domain.MemberErrorCode;
import com.example.member.domain.MemberRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberActivationServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    TimeProvider timeProvider;

    @InjectMocks
    MemberActivationService memberActivationService;

    @Test
    @DisplayName("올바른 코드로 회원을 활성화한다")
    void activateWithValidCode() {
        Instant now = Instant.parse("2024-03-01T00:00:00Z");
        Member member = Member.register(
                new Email("activate@demo.com"),
                "Activate Me",
                "hashed",
                "code-777",
                now.plusSeconds(3600));
        given(memberRepository.findByEmail(new Email("activate@demo.com"))).willReturn(Optional.of(member));
        given(memberRepository.save(member)).willReturn(member);
        given(timeProvider.now()).willReturn(now);

        Member activated = memberActivationService.activate(new MemberActivationCommand("activate@demo.com", "code-777"));

        assertThat(activated.getStatus()).isEqualTo(com.example.member.domain.MemberStatus.ACTIVE);
        assertThat(activated.getActivationCode()).isNull();
        then(memberRepository).should().save(member);
    }

    @Test
    @DisplayName("잘못된 코드로는 활성화되지 않는다")
    void activateFailsWithWrongCode() {
        Instant now = Instant.parse("2024-03-01T00:00:00Z");
        Member member = Member.register(
                new Email("wrong@demo.com"),
                "Wrong Code",
                "hashed",
                "code-888",
                now.plusSeconds(3600));
        given(memberRepository.findByEmail(new Email("wrong@demo.com"))).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberActivationService.activate(new MemberActivationCommand("wrong@demo.com", "nope")))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).errorCode())
                .isEqualTo(MemberErrorCode.ACTIVATION_INVALID);
    }

    @Test
    @DisplayName("만료된 코드로는 활성화되지 않는다")
    void activateFailsWhenExpired() {
        Instant now = Instant.parse("2024-03-01T02:00:00Z");
        Member member = Member.register(
                new Email("expired@demo.com"),
                "Expired",
                "hashed",
                "code-999",
                Instant.parse("2024-03-01T01:00:00Z"));
        given(memberRepository.findByEmail(new Email("expired@demo.com"))).willReturn(Optional.of(member));
        given(timeProvider.now()).willReturn(now);

        assertThatThrownBy(() -> memberActivationService.activate(new MemberActivationCommand("expired@demo.com", "code-999")))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).errorCode())
                .isEqualTo(MemberErrorCode.ACTIVATION_EXPIRED);
    }
}
