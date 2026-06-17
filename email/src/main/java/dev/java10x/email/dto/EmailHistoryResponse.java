package dev.java10x.email.dto;

import dev.java10x.email.enums.EmailStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmailHistoryResponse(
        UUID emailId,
        UUID userId,
        String emailFrom,
        String emailTo,
        String emailSubject,
        String body,
        String originEventType,
        EmailStatus statusEmail,
        Integer attempts,
        LocalDateTime createdAt,
        LocalDateTime lastAttemptAt,
        LocalDateTime sendDateEmail,
        String errorMessage
) {
}
