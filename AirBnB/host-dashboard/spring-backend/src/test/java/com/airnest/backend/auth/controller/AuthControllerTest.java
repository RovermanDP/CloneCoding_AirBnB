package com.airnest.backend.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.airnest.backend.auth.dto.AuthenticatedUserResponse;
import com.airnest.backend.auth.dto.LoginResponse;
import com.airnest.backend.auth.security.JwtAuthenticationFilter;
import com.airnest.backend.auth.service.AuthService;
import com.airnest.backend.common.exception.ApiErrorResponseFactory;
import com.airnest.backend.common.exception.GlobalExceptionHandler;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, ApiErrorResponseFactory.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void loginReturnsJwtPayload() throws Exception {
        when(authService.login(any())).thenReturn(
            new LoginResponse(
                "token-value",
                "Bearer",
                Instant.parse("2026-03-26T10:00:00Z"),
                "refresh-token-value",
                Instant.parse("2026-04-02T10:00:00Z"),
                new AuthenticatedUserResponse(1L, "host@airnest.local", "Emiel Jacobs", "HOST")
            )
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "host@airnest.local",
                      "password": "host1234!"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("token-value"))
            .andExpect(jsonPath("$.user.email").value("host@airnest.local"))
            .andExpect(jsonPath("$.user.role").value("HOST"));
    }
}
