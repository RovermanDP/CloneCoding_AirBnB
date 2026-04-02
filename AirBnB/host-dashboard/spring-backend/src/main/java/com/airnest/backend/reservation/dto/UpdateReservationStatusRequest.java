package com.airnest.backend.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to update reservation status")
public record UpdateReservationStatusRequest(
    @Schema(description = "New reservation status", example = "Ready", allowableValues = {"Preparing", "Ready", "Checked in", "Checked out"}, required = true)
    @NotBlank(message = "Reservation status is invalid.")
    String status
) {
}
