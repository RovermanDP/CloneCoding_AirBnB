package com.airnest.backend.inbox.dto;

import java.util.List;

public record InboxListResponse(
    List<InboxThreadResponse> threads,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last
) {
}
