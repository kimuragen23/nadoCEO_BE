package com.nadoceo.knowledge.presentation.dto;

import com.nadoceo.knowledge.domain.Faq;

import java.time.Instant;
import java.util.UUID;

public record FaqResponse(
        UUID id,
        UUID courseId,
        String question,
        String answer,
        int upvotes,
        String source,
        Instant createdAt
) {
    public static FaqResponse from(Faq faq) {
        return new FaqResponse(
                faq.getId(), faq.getCourseId(),
                faq.getQuestion(), faq.getAnswer(),
                faq.getUpvotes(), faq.getSource().name().toLowerCase(),
                faq.getCreatedAt()
        );
    }
}
