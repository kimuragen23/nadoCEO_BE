package com.nadoceo.analytics.presentation;

import com.nadoceo.analytics.application.GetTimelineUseCase;
import com.nadoceo.analytics.infrastructure.persistence.SpringDataLearningPathRepo;
import com.nadoceo.analytics.presentation.dto.TimelineResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Learning Path", description = "학습 경로 타임라인 API (학생 복습용)")
@RestController
@RequestMapping("/api/v1/learning-path")
public class LearningPathController {

    private final GetTimelineUseCase getTimeline;
    private final SpringDataLearningPathRepo lpRepo;

    public LearningPathController(GetTimelineUseCase getTimeline, SpringDataLearningPathRepo lpRepo) {
        this.getTimeline = getTimeline;
        this.lpRepo = lpRepo;
    }

    @Operation(summary = "세션 타임라인 조회")
    @GetMapping("/{sessionId}")
    public ResponseEntity<List<TimelineResponse>> getSessionTimeline(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(getTimeline.bySession(sessionId));
    }

    @Operation(summary = "내 학습 경로 목록")
    @GetMapping("/my")
    public ResponseEntity<List<TimelineResponse>> getMyPath(@RequestParam UUID studentId) {
        return ResponseEntity.ok(getTimeline.byStudent(studentId));
    }

    @Operation(summary = "내 용어집", description = "학생이 검색한 모든 용어 (중복 제거)")
    @GetMapping("/glossary")
    public ResponseEntity<List<Map<String, Object>>> getGlossary(@RequestParam UUID studentId) {
        var terms = lpRepo.findTermsByStudent(studentId);
        var result = terms.stream().map(row -> Map.<String, Object>of(
                "term", (String) row[0],
                "lastSearchedAt", ((Timestamp) row[1]).toInstant().toString()
        )).toList();
        return ResponseEntity.ok(result);
    }
}
