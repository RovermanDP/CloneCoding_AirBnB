package com.airnest.backend.reservation.dto;

import java.util.List;

public record ReservationListResponse(
    List<ReservationResponse> reservations,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last
) {
}
