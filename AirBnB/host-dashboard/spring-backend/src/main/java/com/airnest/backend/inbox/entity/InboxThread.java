package com.airnest.backend.inbox.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "inbox_threads")
public class InboxThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String guest;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String stay;

    @Column(nullable = false)
    private String room;

    @Convert(converter = InboxThreadStatusConverter.class)
    @Column(nullable = false)
    private InboxThreadStatus status;

    @Column(name = "last_reply")
    private String lastReply;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InboxMessage> messages = new ArrayList<>();

    protected InboxThread() {
    }

    private InboxThread(
        String guest,
        String title,
        String stay,
        String room,
        InboxThreadStatus status,
        String lastReply,
        Instant updatedAt
    ) {
        this.guest = guest;
        this.title = title;
        this.stay = stay;
        this.room = room;
        this.status = status;
        this.lastReply = lastReply;
        this.updatedAt = updatedAt;
    }

    public static InboxThread create(
        String guest,
        String title,
        String stay,
        String room,
        InboxThreadStatus status,
        String lastReply,
        Instant updatedAt
    ) {
        return new InboxThread(guest, title, stay, room, status, lastReply, updatedAt);
    }

    public void addMessage(MessageSender sender, String body, Instant createdAt) {
        InboxMessage message = InboxMessage.create(this, sender, body, createdAt);
        messages.add(message);
    }

    public void reply(String message, Instant repliedAt) {
        this.status = InboxThreadStatus.REPLIED;
        this.lastReply = message;
        this.updatedAt = repliedAt;
        addMessage(MessageSender.HOST, message, repliedAt);
    }

    public Long getId() {
        return id;
    }

    public String getGuest() {
        return guest;
    }

    public String getTitle() {
        return title;
    }

    public String getStay() {
        return stay;
    }

    public String getRoom() {
        return room;
    }

    public InboxThreadStatus getStatus() {
        return status;
    }

    public String getLastReply() {
        return lastReply;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
