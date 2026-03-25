package com.nadoceo.analytics.domain;

import java.util.List;
import java.util.UUID;

public interface LearningPathRepository {

    LearningPathEvent save(LearningPathEvent event);

    List<LearningPathEvent> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    List<LearningPathEvent> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    List<Object[]> findTopSearchedTerms(UUID courseId, int limit);

    List<Object[]> findFaqHitRateByMonth(UUID courseId);

    List<Object[]> findStudentEventStats(UUID courseId);
}
