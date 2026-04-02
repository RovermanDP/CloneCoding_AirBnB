package com.airnest.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token request")
public record RefreshRequest(
    @NotBlank
    @Schema(description = "Opaque refresh token received at login")
    String refreshToken
) {
}
