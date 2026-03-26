package com.airnest.backend.reservation.repository;

import com.airnest.backend.reservation.entity.Reservation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByUpdatedAtAscIdAsc();
}
