package com.airnest.backend.reservation.dto;

import com.airnest.backend.reservation.entity.Reservation;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ReservationResponse(
    Long id,
    String guest,
    String property,
    LocalDate arrivalDate,
    BigDecimal payoutAmount,
    String status,
    Instant updatedAt
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getGuest(),
            reservation.getProperty(),
            reservation.getArrivalDate(),
            reservation.getPayoutAmount(),
            reservation.getStatus().getValue(),
            reservation.getUpdatedAt()
        );
    }
}
