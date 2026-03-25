package com.nadoceo.analytics.application;

import com.nadoceo.analytics.domain.LearningPathEvent;
import com.nadoceo.analytics.domain.LearningPathRepository;
import com.nadoceo.analytics.presentation.dto.TimelineResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetTimelineUseCase {

    private final LearningPathRepository repository;

    public GetTimelineUseCase(LearningPathRepository repository) {
        this.repository = repository;
    }

    public List<TimelineResponse> bySession(UUID sessionId) {
        return repository.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream().map(TimelineResponse::from).toList();
    }

    public List<TimelineResponse> byStudent(UUID studentId) {
        return repository.findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream().map(TimelineResponse::from).toList();
    }
}
