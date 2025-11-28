package com.example.member.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.core.domain.DomainException;
import com.example.core.time.TimeProvider;
import com.example.member.domain.Email;
import com.example.member.domain.Member;
import com.example.member.domain.MemberRepository;
import com.example.member.domain.PasswordHasher;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberCommandServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    PasswordHasher passwordHasher;

    @Mock
    TimeProvider timeProvider;

    @InjectMocks
    MemberCommandService memberCommandService;

    @Test
    void register_successful_when_email_is_unique() {
        MemberRegisterCommand command = new MemberRegisterCommand("new@demo.com", "New User", "secret");
        given(memberRepository.existsByEmail(new Email(command.email()))).willReturn(false);
        given(passwordHasher.hash(command.rawPassword())).willReturn("hashed-secret");
        Instant fixed = Instant.parse("2024-01-01T00:00:00Z");
        given(timeProvider.now()).willReturn(fixed);

        memberCommandService.register(command);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        then(memberRepository).should().save(captor.capture());
        Member saved = captor.getValue();
        assertThat(saved.getEmail().getValue()).isEqualTo("new@demo.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed-secret");
    }

    @Test
    void register_fails_when_email_exists() {
        MemberRegisterCommand command = new MemberRegisterCommand("exists@demo.com", "Existing", "secret");
        given(memberRepository.existsByEmail(new Email(command.email()))).willReturn(true);

        assertThatThrownBy(() -> memberCommandService.register(command))
                .isInstanceOf(DomainException.class);
        then(memberRepository).shouldHaveNoMoreInteractions();
    }
}
