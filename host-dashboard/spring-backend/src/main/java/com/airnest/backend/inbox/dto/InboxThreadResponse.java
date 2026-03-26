package com.airnest.backend.inbox.dto;

import com.airnest.backend.inbox.entity.InboxThread;
import java.time.Instant;

public record InboxThreadResponse(
    Long id,
    String guest,
    String title,
    String stay,
    String room,
    String status,
    String lastReply,
    Instant updatedAt
) {
    public static InboxThreadResponse from(InboxThread thread) {
        return new InboxThreadResponse(
            thread.getId(),
            thread.getGuest(),
            thread.getTitle(),
            thread.getStay(),
            thread.getRoom(),
            thread.getStatus().getValue(),
            thread.getLastReply(),
            thread.getUpdatedAt()
        );
    }
}
