package com.nadoceo.analytics.application;

import com.nadoceo.analytics.domain.LearningPathRepository;
import com.nadoceo.analytics.presentation.dto.AnalyticsResponse;
import com.nadoceo.coaching.domain.ChatSessionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetAnalyticsUseCase {

    private final LearningPathRepository repository;
    private final ChatSessionRepository sessionRepository;

    public GetAnalyticsUseCase(LearningPathRepository repository,
                                ChatSessionRepository sessionRepository) {
        this.repository = repository;
        this.sessionRepository = sessionRepository;
    }

    public List<AnalyticsResponse.TermCount> topTerms(UUID courseId, int limit) {
        return repository.findTopSearchedTerms(courseId, limit)
                .stream()
                .map(row -> new AnalyticsResponse.TermCount(
                        (String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    public double averageTurns(UUID courseId) {
        return sessionRepository.findByCourseIdAndResolved(courseId)
                .stream()
                .mapToInt(s -> s.getTotalTurns())
                .average()
                .orElse(0.0);
    }

    public List<AnalyticsResponse.FaqHitRate> faqHitRate(UUID courseId) {
        return repository.findFaqHitRateByMonth(courseId)
                .stream()
                .map(row -> new AnalyticsResponse.FaqHitRate(
                        ((Timestamp) row[0]).toLocalDateTime().toLocalDate().toString(),
                        ((BigDecimal) row[1]).doubleValue()))
                .toList();
    }

    public Map<String, List<AnalyticsResponse.EventCount>> studentPatterns(UUID courseId) {
        return repository.findStudentEventStats(courseId)
                .stream()
                .collect(Collectors.groupingBy(
                        row -> row[0].toString(),
                        Collectors.mapping(
                                row -> new AnalyticsResponse.EventCount(
                                        (String) row[1], ((Number) row[2]).longValue()),
                                Collectors.toList())));
    }
}
