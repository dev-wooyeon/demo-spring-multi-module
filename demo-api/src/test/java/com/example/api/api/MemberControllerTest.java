package com.example.api.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.api.config.ApiExceptionHandler;
import com.example.member.application.MemberCommandService;
import com.example.member.application.MemberActivationCommand;
import com.example.member.application.MemberActivationService;
import com.example.member.application.MemberQueryService;
import com.example.member.application.MemberRegisterCommand;
import com.example.member.application.MemberSummary;
import com.example.member.domain.Email;
import com.example.member.domain.Member;
import com.example.member.domain.MemberRole;
import com.example.member.domain.MemberStatus;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    MemberCommandService memberCommandService;

    @Mock
    MemberQueryService memberQueryService;

    @Mock
    MemberActivationService memberActivationService;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MemberController controller = new MemberController(
                memberCommandService,
                memberQueryService,
                memberActivationService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("회원 등록 요청이 성공하면 생성된 회원 정보를 반환한다")
    void register_returns_created_member() throws Exception {
        Member member = Member.register(
                new Email("api@demo.com"),
                "Api User",
                "hashed",
                "code",
                Instant.now().plusSeconds(3600));
        given(memberCommandService.register(any(MemberRegisterCommand.class))).willReturn(member);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"api@demo.com","name":"Api User","password":"passw0rd!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("api@demo.com"))
                .andExpect(jsonPath("$.name").value("Api User"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("회원 조회 요청이 성공하면 요약 정보를 반환한다")
    void get_returns_member_summary() throws Exception {
        MemberSummary summary = new MemberSummary(
                1L, "api@demo.com", "Api User", MemberStatus.ACTIVE, Set.of(MemberRole.USER), Instant.now());
        given(memberQueryService.getById(1L)).willReturn(summary);

        mockMvc.perform(get("/api/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("api@demo.com"));
    }

    @Test
    @DisplayName("유효한 코드로 회원을 활성화한다")
    void activate_member() throws Exception {
        Member member = Member.register(
                new Email("act@demo.com"),
                "Activatable",
                "hashed",
                "code-123",
                Instant.now().plusSeconds(3600));
        member.activate("code-123", () -> Instant.now());
        given(memberActivationService.activate(any(MemberActivationCommand.class))).willReturn(member);

        mockMvc.perform(post("/api/members/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"act@demo.com","code":"anything"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("act@demo.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
