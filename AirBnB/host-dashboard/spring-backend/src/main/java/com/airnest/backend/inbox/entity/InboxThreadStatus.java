package com.airnest.backend.inbox.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum InboxThreadStatus {
    AWAITING_REPLY("Awaiting reply"),
    REPLIED("Replied");

    private final String value;

    InboxThreadStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static InboxThreadStatus fromValue(String rawValue) {
        for (InboxThreadStatus status : values()) {
            if (status.value.equalsIgnoreCase(rawValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Inbox thread status is invalid.");
    }
}
