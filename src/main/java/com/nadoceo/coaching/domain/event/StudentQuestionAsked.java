package com.nadoceo.coaching.domain.event;

import com.nadoceo.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record StudentQuestionAsked(
        UUID eventId,
        Instant occurredAt,
        UUID sessionId,
        UUID studentId,
        UUID courseId,
        int turnNumber,
        String message
) implements DomainEvent {
    public StudentQuestionAsked(UUID sessionId, UUID studentId, UUID courseId, int turnNumber, String message) {
        this(UUID.randomUUID(), Instant.now(), sessionId, studentId, courseId, turnNumber, message);
    }
}
