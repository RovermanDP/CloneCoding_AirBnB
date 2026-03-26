package com.airnest.backend.reservation.service;

import com.airnest.backend.common.exception.InvalidRequestException;
import com.airnest.backend.common.exception.ResourceNotFoundException;
import com.airnest.backend.reservation.dto.ReservationListResponse;
import com.airnest.backend.reservation.dto.ReservationResponse;
import com.airnest.backend.reservation.entity.Reservation;
import com.airnest.backend.reservation.entity.ReservationStatus;
import com.airnest.backend.reservation.repository.ReservationRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public ReservationListResponse listReservations() {
        return new ReservationListResponse(
            reservationRepository.findAllByOrderByUpdatedAtAscIdAsc().stream()
                .map(ReservationResponse::from)
                .toList()
        );
    }

    @Transactional
    public ReservationResponse updateStatus(Long reservationId, String rawStatus) {
        ReservationStatus nextStatus;
        try {
            nextStatus = ReservationStatus.fromValue(rawStatus.trim());
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException("Reservation status is invalid.");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation not found."));

        reservation.updateStatus(nextStatus, Instant.now());
        return ReservationResponse.from(reservation);
    }
}
