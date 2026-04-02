package com.airnest.backend.inbox.repository;

import com.airnest.backend.inbox.entity.InboxThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxThreadRepository extends JpaRepository<InboxThread, Long> {

    Page<InboxThread> findAll(Pageable pageable);
}
