package com.airnest.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Logout request")
public record LogoutRequest(
    @Schema(description = "Refresh token to revoke (optional but recommended)")
    String refreshToken
) {
}
