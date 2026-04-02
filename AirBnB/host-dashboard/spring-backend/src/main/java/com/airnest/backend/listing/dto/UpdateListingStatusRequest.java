package com.airnest.backend.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to update listing status")
public record UpdateListingStatusRequest(
    @Schema(description = "New listing status", example = "Published", allowableValues = {"Published", "Draft"}, required = true)
    @NotBlank(message = "Listing status is invalid.")
    String status
) {
}
