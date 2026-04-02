package com.airnest.backend.inbox.entity;

public enum MessageSender {
    HOST("host"),
    GUEST("guest");

    private final String value;

    MessageSender(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MessageSender fromValue(String rawValue) {
        for (MessageSender sender : values()) {
            if (sender.value.equalsIgnoreCase(rawValue)) {
                return sender;
            }
        }
        throw new IllegalArgumentException("Message sender is invalid.");
    }
}
