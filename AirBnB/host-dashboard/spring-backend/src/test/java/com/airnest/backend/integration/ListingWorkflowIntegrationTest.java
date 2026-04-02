package com.airnest.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
@DisplayName("숙소 상태 변경 워크플로우 통합 테스트")
class ListingWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
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
    @DisplayName("시나리오: 숙소 목록 조회 → 상태를 Published에서 Draft로 변경")
    void 숙소_상태를_Published에서_Draft로_변경() throws Exception {
        // Step 1: 숙소 목록 조회
        MvcResult listingsResult = mockMvc.perform(get("/api/listings")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.listings").isArray())
            .andExpect(jsonPath("$.listings").isNotEmpty())
            .andReturn();

        // Step 2: 첫 번째 숙소 ID 추출
        String responseBody = listingsResult.getResponse().getContentAsString();
        Integer firstListingId = JsonPath.read(responseBody, "$.listings[0].id");

        // Step 3: 숙소 상태를 Draft로 변경
        String updateRequest = """
            {
                "status": "Draft"
            }
            """;

        mockMvc.perform(patch("/api/listings/" + firstListingId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.listing.id").value(firstListingId))
            .andExpect(jsonPath("$.listing.status").value("Draft"));

        // Step 4: 숙소 목록 재조회하여 상태 변경 확인
        mockMvc.perform(get("/api/listings")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.listings[?(@.id == " + firstListingId + ")].status").value("Draft"));
    }

    @Test
    @DisplayName("시나리오: 숙소 상태 토글 (Published ↔ Draft)")
    void 숙소_상태_토글() throws Exception {
        // Step 1: 숙소 목록 조회
        MvcResult listingsResult = mockMvc.perform(get("/api/listings")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = listingsResult.getResponse().getContentAsString();
        Integer listingId = JsonPath.read(responseBody, "$.listings[0].id");
        String currentStatus = JsonPath.read(responseBody, "$.listings[0].status");

        // Step 2: 현재 상태에서 반대 상태로 변경
        String newStatus = currentStatus.equals("Published") ? "Draft" : "Published";
        String updateRequest1 = String.format("""
            {
                "status": "%s"
            }
            """, newStatus);

        mockMvc.perform(patch("/api/listings/" + listingId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.listing.status").value(newStatus));

        // Step 3: 다시 원래 상태로 변경
        String updateRequest2 = String.format("""
            {
                "status": "%s"
            }
            """, currentStatus);

        mockMvc.perform(patch("/api/listings/" + listingId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest2))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.listing.status").value(currentStatus));

        // Step 4: 최종 상태 확인
        mockMvc.perform(get("/api/listings")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.listings[?(@.id == " + listingId + ")].status").value(currentStatus));
    }

    @Test
    @DisplayName("시나리오: 여러 숙소의 상태 일괄 변경")
    void 여러_숙소_상태_일괄_변경() throws Exception {
        // Step 1: 숙소 목록 조회
        MvcResult listingsResult = mockMvc.perform(get("/api/listings")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = listingsResult.getResponse().getContentAsString();
        Integer listingCount = JsonPath.read(responseBody, "$.listings.length()");
        int maxUpdates = Math.min(listingCount, 2);

        // Step 2: 처음 2개 숙소를 Draft로 변경
        for (int i = 0; i < maxUpdates; i++) {
            Integer listingId = JsonPath.read(responseBody, "$.listings[" + i + "].id");
            String updateRequest = """
                {
                    "status": "Draft"
                }
                """;

            mockMvc.perform(patch("/api/listings/" + listingId + "/status")
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listing.status").value("Draft"));
        }

        // Step 3: 변경된 상태 확인
        mockMvc.perform(get("/api/listings")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.listings").isArray());
    }

    @Test
    @DisplayName("시나리오: 존재하지 않는 숙소 상태 변경 시도")
    void 존재하지_않는_숙소_상태_변경_시도() throws Exception {
        // Given: 존재하지 않는 숙소 ID
        Long nonExistentId = 999999L;
        String updateRequest = """
            {
                "status": "Published"
            }
            """;

        // When & Then: 404 에러 발생
        mockMvc.perform(patch("/api/listings/" + nonExistentId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("시나리오: 잘못된 숙소 상태로 변경 시도")
    void 잘못된_숙소_상태로_변경_시도() throws Exception {
        // Step 1: 숙소 목록 조회
        MvcResult listingsResult = mockMvc.perform(get("/api/listings")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = listingsResult.getResponse().getContentAsString();
        Integer listingId = JsonPath.read(responseBody, "$.listings[0].id");

        // Step 2: 잘못된 상태로 변경 시도
        String invalidStatusRequest = """
            {
                "status": "InvalidStatus"
            }
            """;

        mockMvc.perform(patch("/api/listings/" + listingId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidStatusRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("시나리오: 빈 상태로 변경 시도")
    void 빈_상태로_변경_시도() throws Exception {
        // Step 1: 숙소 목록 조회
        MvcResult listingsResult = mockMvc.perform(get("/api/listings")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = listingsResult.getResponse().getContentAsString();
        Integer listingId = JsonPath.read(responseBody, "$.listings[0].id");

        // Step 2: 빈 상태로 변경 시도
        String emptyStatusRequest = """
            {
                "status": ""
            }
            """;

        mockMvc.perform(patch("/api/listings/" + listingId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyStatusRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("시나리오: 인증 없이 숙소 상태 변경 시도")
    void 인증_없이_숙소_상태_변경_시도() throws Exception {
        // Given: 상태 변경 요청
        String updateRequest = """
            {
                "status": "Published"
            }
            """;

        // When & Then: 401 에러 발생
        mockMvc.perform(patch("/api/listings/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("시나리오: 잘못된 ID 형식으로 숙소 상태 변경 시도")
    void 잘못된_ID_형식으로_숙소_상태_변경_시도() throws Exception {
        // Given: 잘못된 ID 형식
        String invalidId = "not-a-number";
        String updateRequest = """
            {
                "status": "Published"
            }
            """;

        // When & Then: 400 에러 발생
        mockMvc.perform(patch("/api/listings/" + invalidId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").exists());
    }
}
