package com.airnest.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("메시지 답장 워크플로우 통합 테스트")
class InboxWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // 각 테스트 전에 로그인하여 토큰 획득
        String loginRequest = """
            {
                "email": "host@airnest.local",
                "password": "host1234!"
            }
            """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        authToken = JsonPath.read(responseBody, "$.accessToken");
    }

    @Test
    @DisplayName("시나리오: 메시지 목록 조회 → 첫 번째 메시지에 답장")
    void 메시지_목록_조회_후_답장_전송() throws Exception {
        // Step 1: 메시지 목록 조회
        MvcResult inboxResult = mockMvc.perform(get("/api/inbox")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.threads").isArray())
            .andExpect(jsonPath("$.threads").isNotEmpty())
            .andReturn();

        // Step 2: 첫 번째 메시지 ID 추출
        String inboxResponseBody = inboxResult.getResponse().getContentAsString();
        Integer firstThreadId = JsonPath.read(inboxResponseBody, "$.threads[0].id");
        String guestName = JsonPath.read(inboxResponseBody, "$.threads[0].guest");

        // Step 3: 답장 전송
        String replyRequest = String.format("""
            {
                "message": "Hi %s, thank you for your message! Check-in is at 3 PM."
            }
            """, guestName);

        mockMvc.perform(post("/api/inbox/" + firstThreadId + "/reply")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(replyRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.thread.id").value(firstThreadId))
            .andExpect(jsonPath("$.thread.status").value("Replied"));

        // Step 4: 메시지 목록 재조회하여 상태 변경 확인
        mockMvc.perform(get("/api/inbox")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.threads[?(@.id == " + firstThreadId + ")].status").value("Replied"));
    }

    @Test
    @DisplayName("시나리오: 존재하지 않는 메시지에 답장 시도")
    void 존재하지_않는_메시지에_답장_시도() throws Exception {
        // Given: 존재하지 않는 메시지 ID
        Long nonExistentId = 999999L;
        String replyRequest = """
            {
                "message": "This should fail"
            }
            """;

        // When & Then: 404 에러 발생
        mockMvc.perform(post("/api/inbox/" + nonExistentId + "/reply")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(replyRequest))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("시나리오: 빈 메시지로 답장 시도")
    void 빈_메시지로_답장_시도() throws Exception {
        // Step 1: 첫 번째 메시지 ID 가져오기
        MvcResult inboxResult = mockMvc.perform(get("/api/inbox")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andReturn();

        String inboxResponseBody = inboxResult.getResponse().getContentAsString();
        Integer firstThreadId = JsonPath.read(inboxResponseBody, "$.threads[0].id");

        // Step 2: 빈 메시지로 답장 시도
        String emptyReplyRequest = """
            {
                "message": ""
            }
            """;

        mockMvc.perform(post("/api/inbox/" + firstThreadId + "/reply")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyReplyRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("시나리오: 잘못된 ID 형식으로 답장 시도")
    void 잘못된_ID_형식으로_답장_시도() throws Exception {
        // Given: 잘못된 ID 형식
        String invalidId = "not-a-number";
        String replyRequest = """
            {
                "message": "This should fail"
            }
            """;

        // When & Then: 400 에러 발생
        mockMvc.perform(post("/api/inbox/" + invalidId + "/reply")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(replyRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("시나리오: 인증 없이 메시지 답장 시도")
    void 인증_없이_메시지_답장_시도() throws Exception {
        // Given: 답장 요청
        String replyRequest = """
            {
                "message": "This should fail"
            }
            """;

        // When & Then: 401 에러 발생
        mockMvc.perform(post("/api/inbox/1/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(replyRequest))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("시나리오: 여러 메시지에 연속으로 답장")
    void 여러_메시지에_연속_답장() throws Exception {
        // Step 1: 메시지 목록 조회
        MvcResult inboxResult = mockMvc.perform(get("/api/inbox")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.threads").isArray())
            .andReturn();

        String inboxResponseBody = inboxResult.getResponse().getContentAsString();

        // Step 2: 처음 2개의 메시지에 답장 (있는 경우)
        Integer threadCount = JsonPath.read(inboxResponseBody, "$.threads.length()");
        int maxReplies = Math.min(threadCount, 2);

        for (int i = 0; i < maxReplies; i++) {
            Integer threadId = JsonPath.read(inboxResponseBody, "$.threads[" + i + "].id");
            String guestName = JsonPath.read(inboxResponseBody, "$.threads[" + i + "].guest");

            String replyRequest = String.format("""
                {
                    "message": "Hi %s, thank you for reaching out!"
                }
                """, guestName);

            mockMvc.perform(post("/api/inbox/" + threadId + "/reply")
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(replyRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thread.status").value("Replied"));
        }

        // Step 3: 메시지 목록 재조회하여 상태 확인
        mockMvc.perform(get("/api/inbox")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.threads").isArray());
    }
}
