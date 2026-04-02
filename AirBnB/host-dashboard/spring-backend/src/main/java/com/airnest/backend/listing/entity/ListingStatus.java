package com.airnest.backend.listing.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ListingStatus {
    PUBLISHED("Published"),
    DRAFT("Draft");

    private final String value;

    ListingStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ListingStatus fromValue(String rawValue) {
        for (ListingStatus status : values()) {
            if (status.value.equalsIgnoreCase(rawValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Listing status is invalid.");
    }
}
