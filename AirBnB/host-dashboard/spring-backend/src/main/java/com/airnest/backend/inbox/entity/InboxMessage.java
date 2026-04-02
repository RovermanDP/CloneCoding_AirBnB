package com.airnest.backend.inbox.entity;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "inbox_messages")
public class InboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thread_id", nullable = false)
    private InboxThread thread;

    @Convert(converter = MessageSenderConverter.class)
    @Column(nullable = false)
    private MessageSender sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected InboxMessage() {
    }

    private InboxMessage(InboxThread thread, MessageSender sender, String body, Instant createdAt) {
        this.thread = thread;
        this.sender = sender;
        this.body = body;
        this.createdAt = createdAt;
    }

    public static InboxMessage create(InboxThread thread, MessageSender sender, String body, Instant createdAt) {
        return new InboxMessage(thread, sender, body, createdAt);
    }
}
