package com.airnest.backend.listing.dto;

import com.airnest.backend.listing.entity.Listing;
import java.math.BigDecimal;
import java.time.Instant;

public record ListingResponse(
    Long id,
    String name,
    BigDecimal priceAmount,
    String location,
    String status,
    Instant updatedAt
) {
    public static ListingResponse from(Listing listing) {
        return new ListingResponse(
            listing.getId(),
            listing.getName(),
            listing.getPriceAmount(),
            listing.getLocation(),
            listing.getStatus().getValue(),
            listing.getUpdatedAt()
        );
    }
}
