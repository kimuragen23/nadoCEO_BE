package com.nadoceo.coaching.domain.event;

import com.nadoceo.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record FaqSearchRequested(
        UUID eventId,
        Instant occurredAt,
        UUID sessionId,
        UUID studentId,
        UUID courseId,
        int turnNumber,
        String query,
        boolean hit,
        UUID faqId,
        double similarity
) implements DomainEvent {

    public static FaqSearchRequested hit(UUID sessionId, UUID studentId, UUID courseId,
                                          int turnNumber, String query, UUID faqId, double similarity) {
        return new FaqSearchRequested(UUID.randomUUID(), Instant.now(),
                sessionId, studentId, courseId, turnNumber, query, true, faqId, similarity);
    }

    public static FaqSearchRequested miss(UUID sessionId, UUID studentId, UUID courseId,
                                           int turnNumber, String query) {
        return new FaqSearchRequested(UUID.randomUUID(), Instant.now(),
                sessionId, studentId, courseId, turnNumber, query, false, null, 0.0);
    }
}
