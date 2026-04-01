package com.airnest.backend.reservation.repository;

import com.airnest.backend.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findAll(Pageable pageable);
}
