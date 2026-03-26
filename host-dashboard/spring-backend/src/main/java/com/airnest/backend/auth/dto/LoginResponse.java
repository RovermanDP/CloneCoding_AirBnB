package com.airnest.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "User login response with access token")
public record LoginResponse(
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,

    @Schema(description = "Token type", example = "Bearer")
    String tokenType,

    @Schema(description = "Token expiration timestamp", example = "2024-12-31T23:59:59Z")
    Instant expiresAt,

    @Schema(description = "Authenticated user information")
    AuthenticatedUserResponse user
) {
}
