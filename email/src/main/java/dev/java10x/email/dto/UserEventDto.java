package dev.java10x.email.dto;

import java.util.UUID;

public record UserEventDto(
        UUID userId,
        String name,
        String email,
        String eventType
) {
}
