package com.nadoceo.coaching.domain.event;

import com.nadoceo.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record SessionResolved(
        UUID eventId,
        Instant occurredAt,
        UUID sessionId,
        UUID studentId,
        UUID courseId,
        int totalTurns,
        String summary
) implements DomainEvent {
    public SessionResolved(UUID sessionId, UUID studentId, UUID courseId, int totalTurns, String summary) {
        this(UUID.randomUUID(), Instant.now(), sessionId, studentId, courseId, totalTurns, summary);
    }
}
