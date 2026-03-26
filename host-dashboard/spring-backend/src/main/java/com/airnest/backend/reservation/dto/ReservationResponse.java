package com.airnest.backend.reservation.dto;

import com.airnest.backend.reservation.entity.Reservation;
import java.time.Instant;

public record ReservationResponse(
    Long id,
    String guest,
    String property,
    String arrival,
    String payout,
    String status,
    Instant updatedAt
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getGuest(),
            reservation.getProperty(),
            reservation.getArrival(),
            reservation.getPayout(),
            reservation.getStatus().getValue(),
            reservation.getUpdatedAt()
        );
    }
}
