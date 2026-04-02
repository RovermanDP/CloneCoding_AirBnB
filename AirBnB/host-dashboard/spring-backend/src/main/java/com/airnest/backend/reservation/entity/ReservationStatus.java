package com.airnest.backend.reservation.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ReservationStatus {
    PREPARING("Preparing"),
    READY("Ready"),
    CHECKED_IN("Checked in"),
    CHECKED_OUT("Checked out");

    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ReservationStatus fromValue(String rawValue) {
        for (ReservationStatus status : values()) {
            if (status.value.equalsIgnoreCase(rawValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Reservation status is invalid.");
    }
}
