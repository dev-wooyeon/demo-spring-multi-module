package com.example.api.api;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.member.domain.Email;
import com.example.member.domain.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AuthIntegrationTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    MemberRepository memberRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("회원 활성화 후 발급된 토큰으로 보호된 API를 호출한다")
    void activateThenLoginAndAccessProtectedResource() throws Exception {
        String email = "integration+" + System.currentTimeMillis() + "@demo.com";
        String password = "passw0rd!";

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","name":"Flow User","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        String activationCode = memberRepository.findByEmail(new Email(email))
                .orElseThrow()
                .getActivationCode();

        mockMvc.perform(post("/api/members/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","code":"%s"}
                                """.formatted(email, activationCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode tokenJson = new com.fasterxml.jackson.databind.ObjectMapper().readTree(loginResponse);
        String accessToken = tokenJson.get("accessToken").asText();
        Long memberId = memberRepository.findByEmail(new Email(email)).orElseThrow().getId();

        mockMvc.perform(get("/api/members/" + memberId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("잘못된 토큰으로는 보호된 API에 접근할 수 없다")
    void rejectsInvalidToken() throws Exception {
        mockMvc.perform(get("/api/members/1")
                        .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized());
    }
}
