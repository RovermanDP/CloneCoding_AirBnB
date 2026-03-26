package com.airnest.backend.listing.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.airnest.backend.auth.security.JwtAuthenticationFilter;
import com.airnest.backend.common.exception.ApiErrorResponseFactory;
import com.airnest.backend.common.exception.GlobalExceptionHandler;
import com.airnest.backend.listing.dto.ListingResponse;
import com.airnest.backend.listing.service.ListingService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ListingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, ApiErrorResponseFactory.class})
class ListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListingService listingService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void updateStatusReturnsUpdatedListing() throws Exception {
        when(listingService.updateStatus(eq(1L), eq("Published"))).thenReturn(
            new ListingResponse(1L, "Room near city centre", "KRW 179,000", "Seoul", "Published", Instant.parse("2026-03-26T10:00:00Z"))
        );

        mockMvc.perform(patch("/api/listings/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "Published"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.listing.status").value("Published"))
            .andExpect(jsonPath("$.listing.id").value(1));
    }

    @Test
    void updateStatusRejectsInvalidIdentifier() throws Exception {
        mockMvc.perform(patch("/api/listings/abc/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "Published"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("invalid_request"))
            .andExpect(jsonPath("$.error").value("Invalid listing id."));
    }
}
