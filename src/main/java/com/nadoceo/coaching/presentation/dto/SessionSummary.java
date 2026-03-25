package com.nadoceo.coaching.presentation.dto;

import com.nadoceo.coaching.domain.ChatSession;

import java.time.Instant;
import java.util.UUID;

public record SessionSummary(
        UUID id,
        String title,
        int totalTurns,
        boolean resolved,
        Instant createdAt
) {
    public static SessionSummary from(ChatSession session) {
        return new SessionSummary(
                session.getId(),
                "코칭 세션 #" + session.getId().toString().substring(0, 8),
                session.getTotalTurns(),
                session.isResolved(),
                session.getCreatedAt()
        );
    }
}
