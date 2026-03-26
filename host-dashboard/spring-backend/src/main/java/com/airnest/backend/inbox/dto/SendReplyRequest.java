package com.airnest.backend.inbox.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to send a reply to a guest message")
public record SendReplyRequest(
    @Schema(description = "Reply message content", example = "Thank you for your message! Check-in is at 3 PM.", required = true)
    @NotBlank(message = "Reply message is required.")
    String message
) {
}
