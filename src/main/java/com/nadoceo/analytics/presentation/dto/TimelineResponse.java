package com.nadoceo.analytics.presentation.dto;

import com.nadoceo.analytics.domain.LearningPathEvent;

import java.time.Instant;
import java.util.UUID;

public record TimelineResponse(
        UUID id,
        UUID sessionId,
        String eventType,
        int turnNumber,
        String content,
        UUID faqHitId,
        String metadata,
        Instant createdAt
) {
    public static TimelineResponse from(LearningPathEvent event) {
        return new TimelineResponse(
                event.getId(), event.getSessionId(),
                event.getEventType().name(), event.getTurnNumber(),
                event.getContent(), event.getFaqHitId(),
                event.getMetadata(), event.getCreatedAt());
    }
}
