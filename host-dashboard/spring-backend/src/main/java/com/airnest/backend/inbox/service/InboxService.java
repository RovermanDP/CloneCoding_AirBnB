package com.airnest.backend.inbox.service;

import com.airnest.backend.common.exception.ResourceNotFoundException;
import com.airnest.backend.inbox.dto.InboxListResponse;
import com.airnest.backend.inbox.dto.InboxThreadResponse;
import com.airnest.backend.inbox.entity.InboxThread;
import com.airnest.backend.inbox.repository.InboxThreadRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InboxService {

    private final InboxThreadRepository inboxThreadRepository;

    public InboxService(InboxThreadRepository inboxThreadRepository) {
        this.inboxThreadRepository = inboxThreadRepository;
    }

    @Transactional(readOnly = true)
    public InboxListResponse listThreads() {
        return new InboxListResponse(
            inboxThreadRepository.findAllByOrderByUpdatedAtDescIdDesc().stream()
                .map(InboxThreadResponse::from)
                .toList()
        );
    }

    @Transactional
    public InboxThreadResponse sendReply(Long threadId, String message) {
        InboxThread thread = inboxThreadRepository.findById(threadId)
            .orElseThrow(() -> new ResourceNotFoundException("Inbox thread not found."));

        thread.reply(message.trim(), Instant.now());
        return InboxThreadResponse.from(thread);
    }
}
