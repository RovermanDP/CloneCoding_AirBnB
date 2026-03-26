package com.airnest.backend.inbox.repository;

import com.airnest.backend.inbox.entity.InboxThread;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxThreadRepository extends JpaRepository<InboxThread, Long> {

    List<InboxThread> findAllByOrderByUpdatedAtDescIdDesc();
}
