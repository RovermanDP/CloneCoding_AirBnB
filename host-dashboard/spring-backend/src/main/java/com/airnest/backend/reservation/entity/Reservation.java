package com.airnest.backend.reservation.entity;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String guest;

    @Column(nullable = false)
    private String property;

    @Column(nullable = false)
    private String arrival;

    @Column(nullable = false)
    private String payout;

    @Convert(converter = ReservationStatusConverter.class)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Reservation() {
    }

    private Reservation(
        String guest,
        String property,
        String arrival,
        String payout,
        ReservationStatus status,
        Instant updatedAt
    ) {
        this.guest = guest;
        this.property = property;
        this.arrival = arrival;
        this.payout = payout;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public static Reservation create(
        String guest,
        String property,
        String arrival,
        String payout,
        ReservationStatus status,
        Instant updatedAt
    ) {
        return new Reservation(guest, property, arrival, payout, status, updatedAt);
    }

    public void updateStatus(ReservationStatus nextStatus, Instant updatedAt) {
        this.status = nextStatus;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getGuest() {
        return guest;
    }

    public String getProperty() {
        return property;
    }

    public String getArrival() {
        return arrival;
    }

    public String getPayout() {
        return payout;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
