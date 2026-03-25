package com.nadoceo.analytics.infrastructure.persistence;

import com.nadoceo.analytics.domain.LearningPathEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataLearningPathRepo extends JpaRepository<LearningPathEvent, UUID> {

    List<LearningPathEvent> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    List<LearningPathEvent> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    @Query(value = """
            SELECT DISTINCT ON (content) content, created_at
            FROM learning_path_events
            WHERE student_id = :studentId AND event_type = 'TERM_SEARCHED'
            ORDER BY content, created_at DESC
            """, nativeQuery = true)
    List<Object[]> findTermsByStudent(@Param("studentId") UUID studentId);

    @Query(value = """
            SELECT session_id FROM learning_path_events
            WHERE student_id = :studentId AND event_type = 'TERM_SEARCHED' AND content = :term
            ORDER BY created_at DESC LIMIT 1
            """, nativeQuery = true)
    java.util.Optional<UUID> findSessionByTerm(@Param("studentId") UUID studentId, @Param("term") String term);

    @Query(value = """
            SELECT content, COUNT(*) AS search_count
            FROM learning_path_events
            WHERE course_id = :courseId AND event_type = 'TERM_SEARCHED'
            GROUP BY content
            ORDER BY search_count DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findTopSearchedTerms(@Param("courseId") UUID courseId, @Param("limit") int limit);

    @Query(value = """
            SELECT
                DATE_TRUNC('month', created_at) AS month,
                COUNT(*) FILTER (WHERE event_type = 'FAQ_HIT') * 100.0 / COUNT(*) AS hit_rate
            FROM learning_path_events
            WHERE course_id = :courseId
              AND event_type IN ('FAQ_HIT', 'FAQ_MISS')
            GROUP BY month
            ORDER BY month
            """, nativeQuery = true)
    List<Object[]> findFaqHitRateByMonth(@Param("courseId") UUID courseId);

    @Query(value = """
            SELECT student_id, event_type, COUNT(*) AS event_count
            FROM learning_path_events
            WHERE course_id = :courseId
            GROUP BY student_id, event_type
            ORDER BY student_id
            """, nativeQuery = true)
    List<Object[]> findStudentEventStats(@Param("courseId") UUID courseId);
}
