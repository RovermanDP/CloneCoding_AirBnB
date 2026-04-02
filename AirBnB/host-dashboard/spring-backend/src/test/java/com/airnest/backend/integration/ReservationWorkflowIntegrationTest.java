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
@DisplayName("예약 상태 변경 워크플로우 통합 테스트")
class ReservationWorkflowIntegrationTest {

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
    @DisplayName("시나리오: 예약 목록 조회 → 상태를 Preparing에서 Ready로 변경")
    void 예약_상태를_Preparing에서_Ready로_변경() throws Exception {
        // Step 1: 예약 목록 조회
        MvcResult reservationsResult = mockMvc.perform(get("/api/reservations")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reservations").isArray())
            .andExpect(jsonPath("$.reservations").isNotEmpty())
            .andReturn();

        // Step 2: 첫 번째 예약 ID 추출
        String responseBody = reservationsResult.getResponse().getContentAsString();
        Integer firstReservationId = JsonPath.read(responseBody, "$.reservations[0].id");

        // Step 3: 예약 상태를 Ready로 변경
        String updateRequest = """
            {
                "status": "Ready"
            }
            """;

        mockMvc.perform(patch("/api/reservations/" + firstReservationId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reservation.id").value(firstReservationId))
            .andExpect(jsonPath("$.reservation.status").value("Ready"));

        // Step 4: 예약 목록 재조회하여 상태 변경 확인
        mockMvc.perform(get("/api/reservations")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reservations[?(@.id == " + firstReservationId + ")].status").value("Ready"));
    }

    @Test
    @DisplayName("시나리오: 예약 상태를 순차적으로 변경 (Preparing → Ready → Checked in → Checked out)")
    void 예약_상태_순차_변경() throws Exception {
        // Step 1: 예약 목록 조회
        MvcResult reservationsResult = mockMvc.perform(get("/api/reservations")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = reservationsResult.getResponse().getContentAsString();
        Integer reservationId = JsonPath.read(responseBody, "$.reservations[0].id");

        // Step 2: Preparing → Ready
        String readyRequest = """
            {
                "status": "Ready"
            }
            """;

        mockMvc.perform(patch("/api/reservations/" + reservationId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(readyRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reservation.status").value("Ready"));

        // Step 3: Ready → Checked in
        String checkedInRequest = """
            {
                "status": "Checked in"
            }
            """;

        mockMvc.perform(patch("/api/reservations/" + reservationId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(checkedInRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reservation.status").value("Checked in"));

        // Step 4: Checked in → Checked out
        String checkedOutRequest = """
            {
                "status": "Checked out"
            }
            """;

        mockMvc.perform(patch("/api/reservations/" + reservationId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(checkedOutRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reservation.status").value("Checked out"));

        // Step 5: 최종 상태 확인
        mockMvc.perform(get("/api/reservations")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reservations[?(@.id == " + reservationId + ")].status").value("Checked out"));
    }

    @Test
    @DisplayName("시나리오: 존재하지 않는 예약 상태 변경 시도")
    void 존재하지_않는_예약_상태_변경_시도() throws Exception {
        // Given: 존재하지 않는 예약 ID
        Long nonExistentId = 999999L;
        String updateRequest = """
            {
                "status": "Ready"
            }
            """;

        // When & Then: 404 에러 발생
        mockMvc.perform(patch("/api/reservations/" + nonExistentId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("시나리오: 잘못된 예약 상태로 변경 시도")
    void 잘못된_예약_상태로_변경_시도() throws Exception {
        // Step 1: 예약 목록 조회
        MvcResult reservationsResult = mockMvc.perform(get("/api/reservations")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = reservationsResult.getResponse().getContentAsString();
        Integer reservationId = JsonPath.read(responseBody, "$.reservations[0].id");

        // Step 2: 잘못된 상태로 변경 시도
        String invalidStatusRequest = """
            {
                "status": "InvalidStatus"
            }
            """;

        mockMvc.perform(patch("/api/reservations/" + reservationId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidStatusRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("시나리오: 빈 상태로 변경 시도")
    void 빈_상태로_변경_시도() throws Exception {
        // Step 1: 예약 목록 조회
        MvcResult reservationsResult = mockMvc.perform(get("/api/reservations")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = reservationsResult.getResponse().getContentAsString();
        Integer reservationId = JsonPath.read(responseBody, "$.reservations[0].id");

        // Step 2: 빈 상태로 변경 시도
        String emptyStatusRequest = """
            {
                "status": ""
            }
            """;

        mockMvc.perform(patch("/api/reservations/" + reservationId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyStatusRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("시나리오: 인증 없이 예약 상태 변경 시도")
    void 인증_없이_예약_상태_변경_시도() throws Exception {
        // Given: 상태 변경 요청
        String updateRequest = """
            {
                "status": "Ready"
            }
            """;

        // When & Then: 401 에러 발생
        mockMvc.perform(patch("/api/reservations/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("시나리오: 잘못된 ID 형식으로 예약 상태 변경 시도")
    void 잘못된_ID_형식으로_예약_상태_변경_시도() throws Exception {
        // Given: 잘못된 ID 형식
        String invalidId = "not-a-number";
        String updateRequest = """
            {
                "status": "Ready"
            }
            """;

        // When & Then: 400 에러 발생
        mockMvc.perform(patch("/api/reservations/" + invalidId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").exists());
    }
}
