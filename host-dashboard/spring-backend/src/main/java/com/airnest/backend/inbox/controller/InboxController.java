package com.airnest.backend.inbox.controller;

import com.airnest.backend.common.exception.InvalidRequestException;
import com.airnest.backend.inbox.dto.InboxListResponse;
import com.airnest.backend.inbox.dto.SendReplyRequest;
import com.airnest.backend.inbox.dto.SendReplyResponse;
import com.airnest.backend.inbox.service.InboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Inbox", description = "Guest message and conversation management APIs")
@SecurityRequirement(name = "bearerAuth")
@Validated
@RestController
@RequestMapping("/api/inbox")
public class InboxController {

    private final InboxService inboxService;

    public InboxController(InboxService inboxService) {
        this.inboxService = inboxService;
    }

    @Operation(
        summary = "List all inbox threads",
        description = "Retrieve all guest conversation threads for the authenticated host"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Threads retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public InboxListResponse listThreads(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @Parameter(description = "페이지 크기 (최대 100)", example = "20")
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return inboxService.listThreads(page, size);
    }

    @Operation(
        summary = "Reply to a guest message",
        description = "Send a reply message to a specific guest conversation thread"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reply sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Thread not found")
    })
    @PostMapping("/{id}/reply")
    public SendReplyResponse sendReply(
        @Parameter(description = "Thread ID", example = "1") @PathVariable("id") String id,
        @Valid @RequestBody SendReplyRequest request
    ) {
        return new SendReplyResponse(inboxService.sendReply(parseId(id, "Invalid inbox thread id."), request.message()));
    }

    private Long parseId(String rawId, String errorMessage) {
        try {
            long numericId = Long.parseLong(rawId);
            if (numericId <= 0) {
                throw new InvalidRequestException(errorMessage);
            }
            return numericId;
        } catch (NumberFormatException exception) {
            throw new InvalidRequestException(errorMessage);
        }
    }
}
