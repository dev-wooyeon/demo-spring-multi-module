package com.example.api.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.api.config.ApiExceptionHandler;
import com.example.member.application.MemberCommandService;
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

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MemberController controller = new MemberController(memberCommandService, memberQueryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void register_returns_created_member() throws Exception {
        Member member = Member.register(new Email("api@demo.com"), "Api User", "hashed", () -> Instant.now());
        given(memberCommandService.register(any(MemberRegisterCommand.class))).willReturn(member);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"api@demo.com","name":"Api User","password":"passw0rd!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("api@demo.com"))
                .andExpect(jsonPath("$.name").value("Api User"));
    }

    @Test
    void get_returns_member_summary() throws Exception {
        MemberSummary summary = new MemberSummary(
                1L, "api@demo.com", "Api User", MemberStatus.ACTIVE, Set.of(MemberRole.USER), Instant.now());
        given(memberQueryService.getById(1L)).willReturn(summary);

        mockMvc.perform(get("/api/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("api@demo.com"));
    }
}
