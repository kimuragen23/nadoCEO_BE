package com.nadoceo.coaching.domain.event;

import com.nadoceo.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record TermLookupRequested(
        UUID eventId,
        Instant occurredAt,
        UUID sessionId,
        UUID studentId,
        UUID courseId,
        int turnNumber,
        String term
) implements DomainEvent {
    public TermLookupRequested(UUID sessionId, UUID studentId, UUID courseId, int turnNumber, String term) {
        this(UUID.randomUUID(), Instant.now(), sessionId, studentId, courseId, turnNumber, term);
    }
}
