package com.airnest.backend.listing.dto;

import com.airnest.backend.listing.entity.Listing;
import java.time.Instant;

public record ListingResponse(
    Long id,
    String name,
    String price,
    String location,
    String status,
    Instant updatedAt
) {
    public static ListingResponse from(Listing listing) {
        return new ListingResponse(
            listing.getId(),
            listing.getName(),
            listing.getPrice(),
            listing.getLocation(),
            listing.getStatus().getValue(),
            listing.getUpdatedAt()
        );
    }
}
