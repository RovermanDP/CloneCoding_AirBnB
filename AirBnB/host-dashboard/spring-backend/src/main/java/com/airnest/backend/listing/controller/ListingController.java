package com.airnest.backend.listing.controller;

import com.airnest.backend.common.exception.InvalidRequestException;
import com.airnest.backend.listing.dto.ListingListResponse;
import com.airnest.backend.listing.dto.UpdateListingStatusRequest;
import com.airnest.backend.listing.dto.UpdateListingStatusResponse;
import com.airnest.backend.listing.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Listings", description = "Property listing management APIs")
@SecurityRequirement(name = "bearerAuth")
@Validated
@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @Operation(
        summary = "List all listings",
        description = "Retrieve all property listings for the authenticated host"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ListingListResponse listListings(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @Parameter(description = "페이지 크기 (최대 100)", example = "20")
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return listingService.listListings(page, size);
    }

    @Operation(
        summary = "Update listing status",
        description = "Update the publication status of a specific listing (Published, Draft)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Listing not found")
    })
    @PatchMapping("/{id}/status")
    public UpdateListingStatusResponse updateStatus(
        @Parameter(description = "Listing ID", example = "1") @PathVariable("id") String id,
        @Valid @RequestBody UpdateListingStatusRequest request
    ) {
        return new UpdateListingStatusResponse(
            listingService.updateStatus(parseId(id, "Invalid listing id."), request.status())
        );
    }

    private Long parseId(String rawId, String errorMessage) {
        try {
            long numericId = Long.parseLong(rawId);
            if (numericId <= 0) {
                throw new InvalidRequestException(errorMessage);
            }
            return numericId;
        } catch (NumberFormatException exception) {
            throw new InvalidRequestException(errorMessage);
        }
    }
}
