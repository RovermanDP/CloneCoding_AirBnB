package com.airnest.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User login request")
public record LoginRequest(
    @Schema(description = "User email address", example = "host@airnest.local", required = true)
    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be valid.")
    String email,

    @Schema(description = "User password", example = "host1234!", required = true)
    @NotBlank(message = "Password is required.")
    String password
) {
}
