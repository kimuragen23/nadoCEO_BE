package com.nadoceo.analytics.infrastructure.persistence;

import com.nadoceo.analytics.domain.LearningPathEvent;
import com.nadoceo.analytics.domain.LearningPathRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaLearningPathRepository implements LearningPathRepository {

    private final SpringDataLearningPathRepo springDataRepo;

    public JpaLearningPathRepository(SpringDataLearningPathRepo springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public LearningPathEvent save(LearningPathEvent event) {
        return springDataRepo.save(event);
    }

    @Override
    public List<LearningPathEvent> findBySessionIdOrderByCreatedAtAsc(UUID sessionId) {
        return springDataRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Override
    public List<LearningPathEvent> findByStudentIdOrderByCreatedAtDesc(UUID studentId) {
        return springDataRepo.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Override
    public List<Object[]> findTopSearchedTerms(UUID courseId, int limit) {
        return springDataRepo.findTopSearchedTerms(courseId, limit);
    }

    @Override
    public List<Object[]> findFaqHitRateByMonth(UUID courseId) {
        return springDataRepo.findFaqHitRateByMonth(courseId);
    }

    @Override
    public List<Object[]> findStudentEventStats(UUID courseId) {
        return springDataRepo.findStudentEventStats(courseId);
    }
}
