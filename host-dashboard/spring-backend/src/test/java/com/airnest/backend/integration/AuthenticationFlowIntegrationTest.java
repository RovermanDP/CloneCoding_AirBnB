package com.airnest.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@DisplayName("인증 플로우 통합 테스트")
class AuthenticationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("시나리오: 로그인 성공 후 인증된 API 호출")
    void 로그인_후_인증된_API_호출_시나리오() throws Exception {
        // Given: 유효한 로그인 정보
        String loginRequest = """
            {
                "email": "host@airnest.local",
                "password": "host1234!"
            }
            """;

        // When: 로그인 요청
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.user.email").value("host@airnest.local"))
            .andExpect(jsonPath("$.user.role").value("HOST"))
            .andReturn();

        // Then: 토큰 추출
        String responseBody = loginResult.getResponse().getContentAsString();
        String token = JsonPath.read(responseBody, "$.accessToken");

        // When: 토큰으로 인증된 API 호출 (/api/auth/me)
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("host@airnest.local"))
            .andExpect(jsonPath("$.role").value("HOST"));

        // When: 토큰으로 보호된 리소스 접근 (inbox)
        mockMvc.perform(get("/api/inbox")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.threads").isArray());

        // When: 토큰으로 보호된 리소스 접근 (reservations)
        mockMvc.perform(get("/api/reservations")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reservations").isArray());

        // When: 토큰으로 보호된 리소스 접근 (listings)
        mockMvc.perform(get("/api/listings")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.listings").isArray());
    }

    @Test
    @DisplayName("시나리오: 잘못된 비밀번호로 로그인 실패")
    void 잘못된_비밀번호로_로그인_실패() throws Exception {
        // Given: 잘못된 비밀번호
        String loginRequest = """
            {
                "email": "host@airnest.local",
                "password": "wrongpassword"
            }
            """;

        // When & Then: 로그인 실패
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("시나리오: 존재하지 않는 사용자로 로그인 실패")
    void 존재하지_않는_사용자로_로그인_실패() throws Exception {
        // Given: 존재하지 않는 이메일
        String loginRequest = """
            {
                "email": "nonexistent@example.com",
                "password": "password123"
            }
            """;

        // When & Then: 로그인 실패
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("시나리오: 토큰 없이 보호된 API 호출 시 401")
    void 토큰_없이_보호된_API_호출_시_401() throws Exception {
        // When & Then: 인증 없이 보호된 엔드포인트 접근
        mockMvc.perform(get("/api/inbox"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/reservations"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("시나리오: 잘못된 토큰으로 API 호출 시 401")
    void 잘못된_토큰으로_API_호출_시_401() throws Exception {
        // Given: 잘못된 토큰
        String invalidToken = "invalid.jwt.token";

        // When & Then: 잘못된 토큰으로 보호된 엔드포인트 접근
        mockMvc.perform(get("/api/inbox")
                .header("Authorization", "Bearer " + invalidToken))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + invalidToken))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("시나리오: 잘못된 형식의 로그인 요청")
    void 잘못된_형식의_로그인_요청() throws Exception {
        // Given: 이메일 형식 오류
        String invalidEmailRequest = """
            {
                "email": "not-an-email",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidEmailRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        // Given: 빈 비밀번호
        String emptyPasswordRequest = """
            {
                "email": "test@example.com",
                "password": ""
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyPasswordRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
