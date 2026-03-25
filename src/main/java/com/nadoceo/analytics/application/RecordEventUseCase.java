package com.nadoceo.analytics.application;

import com.nadoceo.analytics.domain.EventType;
import com.nadoceo.analytics.domain.LearningPathEvent;
import com.nadoceo.analytics.domain.LearningPathRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RecordEventUseCase {

    private final LearningPathRepository repository;

    public RecordEventUseCase(LearningPathRepository repository) {
        this.repository = repository;
    }

    public void execute(UUID sessionId, UUID studentId, UUID courseId,
                        EventType eventType, int turnNumber, String content,
                        UUID faqHitId, String metadata) {
        var event = new LearningPathEvent(sessionId, studentId, courseId,
                eventType, turnNumber, content, faqHitId, metadata);
        repository.save(event);
    }
}
