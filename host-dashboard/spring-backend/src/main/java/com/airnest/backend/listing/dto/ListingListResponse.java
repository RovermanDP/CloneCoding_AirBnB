package com.airnest.backend.listing.dto;

import java.util.List;

public record ListingListResponse(
    List<ListingResponse> listings,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last
) {
}
