package com.airnest.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@DisplayName("인증 실패 종합 시나리오 통합 테스트")
class UnauthorizedAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("시나리오: 모든 보호된 엔드포인트에 인증 없이 접근 시도")
    void 모든_보호된_엔드포인트_인증_없이_접근() throws Exception {
        // When & Then: 모든 GET 엔드포인트 401 확인
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/inbox"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/reservations"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("시나리오: 모든 변경 엔드포인트에 인증 없이 접근 시도")
    void 모든_변경_엔드포인트_인증_없이_접근() throws Exception {
        // Given: 각 엔드포인트별 요청 본문
        String replyRequest = """
            {
                "message": "Test message"
            }
            """;

        String statusRequest = """
            {
                "status": "Ready"
            }
            """;

        // When & Then: 모든 POST/PATCH 엔드포인트 401 확인
        mockMvc.perform(post("/api/inbox/1/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(replyRequest))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/reservations/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusRequest))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/listings/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusRequest))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("시나리오: 잘못된 Bearer 토큰으로 모든 엔드포인트 접근 시도")
    void 잘못된_Bearer_토큰으로_모든_엔드포인트_접근() throws Exception {
        // Given: 잘못된 토큰
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.token";

        // When & Then: 모든 보호된 엔드포인트 401 확인
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + invalidToken))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/inbox")
                .header("Authorization", "Bearer " + invalidToken))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/reservations")
                .header("Authorization", "Bearer " + invalidToken))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/listings")
                .header("Authorization", "Bearer " + invalidToken))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("시나리오: Authorization 헤더 없이 모든 엔드포인트 접근 시도")
    void Authorization_헤더_없이_모든_엔드포인트_접근() throws Exception {
        // When & Then: Authorization 헤더가 없으면 401
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/inbox"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/reservations"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("시나리오: 잘못된 Authorization 헤더 형식으로 접근 시도")
    void 잘못된_Authorization_헤더_형식으로_접근() throws Exception {
        // Given: 잘못된 Authorization 헤더 형식
        String tokenWithoutBearer = "some-token-value";
        String tokenWithWrongScheme = "Basic some-token-value";

        // When & Then: Bearer 없이 토큰만 제공
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", tokenWithoutBearer))
            .andExpect(status().isUnauthorized());

        // When & Then: Basic 스키마로 제공
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", tokenWithWrongScheme))
            .andExpect(status().isUnauthorized());

        // When & Then: 빈 Bearer 토큰
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer "))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("시나리오: 공개 엔드포인트는 인증 없이 접근 가능")
    void 공개_엔드포인트는_인증_없이_접근_가능() throws Exception {
        // When & Then: 공개 엔드포인트는 200 또는 적절한 응답
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk());

        // POST /api/auth/login은 인증 없이 접근 가능 (잘못된 credential이라도)
        String loginRequest = """
            {
                "email": "test@example.com",
                "password": "wrongpassword"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
            .andExpect(status().isUnauthorized()); // 인증 실패지만, 엔드포인트 자체는 접근 가능
    }

    @Test
    @DisplayName("시나리오: OPTIONS 요청은 모든 경로에서 허용")
    void OPTIONS_요청은_모든_경로에서_허용() throws Exception {
        // When & Then: CORS preflight OPTIONS 요청은 허용
        mockMvc.perform(options("/api/inbox")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk());

        mockMvc.perform(options("/api/reservations")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk());

        mockMvc.perform(options("/api/listings")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("시나리오: 만료된 토큰으로 접근 시도 (시뮬레이션)")
    void 만료된_토큰으로_접근_시도() throws Exception {
        // Given: 만료된 토큰 형식의 문자열 (실제로는 만료되지 않았지만 형식 테스트)
        String expiredLikeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiZXhwIjoxNTE2MjM5MDIyfQ.invalid";

        // When & Then: 401 에러 발생
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + expiredLikeToken))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("시나리오: Swagger UI 경로는 인증 없이 접근 가능")
    void Swagger_UI_경로는_인증_없이_접근_가능() throws Exception {
        // When & Then: Swagger UI 관련 경로는 모두 접근 가능
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk());

        // Note: /swagger-ui/** 는 실제로는 리다이렉트되므로 3xx 응답이 정상
        mockMvc.perform(get("/swagger-ui.html"))
            .andExpect(status().is3xxRedirection());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder options(String urlTemplate, Object... uriVars) {
        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options(urlTemplate, uriVars);
    }
}
