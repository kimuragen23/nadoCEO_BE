package com.nadoceo.coaching.domain.event;

import com.nadoceo.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record SocraticQuestionDelivered(
        UUID eventId,
        Instant occurredAt,
        UUID sessionId,
        UUID studentId,
        UUID courseId,
        int turnNumber
) implements DomainEvent {
    public SocraticQuestionDelivered(UUID sessionId, UUID studentId, UUID courseId, int turnNumber) {
        this(UUID.randomUUID(), Instant.now(), sessionId, studentId, courseId, turnNumber);
    }
}
