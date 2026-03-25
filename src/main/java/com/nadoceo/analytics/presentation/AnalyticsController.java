package com.nadoceo.analytics.presentation;

import com.nadoceo.analytics.application.GetAnalyticsUseCase;
import com.nadoceo.analytics.presentation.dto.AnalyticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Analytics", description = "강사용 학습 분석 API")
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final GetAnalyticsUseCase analytics;

    public AnalyticsController(GetAnalyticsUseCase analytics) {
        this.analytics = analytics;
    }

    @Operation(summary = "가장 많이 막힌 용어 Top N")
    @GetMapping("/{courseId}/terms")
    public ResponseEntity<List<AnalyticsResponse.TermCount>> getTopTerms(
            @PathVariable UUID courseId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analytics.topTerms(courseId, limit));
    }

    @Operation(summary = "평균 해결 턴 수")
    @GetMapping("/{courseId}/turns")
    public ResponseEntity<AnalyticsResponse.AverageTurns> getAverageTurns(@PathVariable UUID courseId) {
        return ResponseEntity.ok(new AnalyticsResponse.AverageTurns(analytics.averageTurns(courseId)));
    }

    @Operation(summary = "FAQ 히트율 추이 (월별)")
    @GetMapping("/{courseId}/faq-hit-rate")
    public ResponseEntity<List<AnalyticsResponse.FaqHitRate>> getFaqHitRate(@PathVariable UUID courseId) {
        return ResponseEntity.ok(analytics.faqHitRate(courseId));
    }

    @Operation(summary = "학생별 학습 패턴")
    @GetMapping("/{courseId}/students")
    public ResponseEntity<Map<String, List<AnalyticsResponse.EventCount>>> getStudentPatterns(
            @PathVariable UUID courseId) {
        return ResponseEntity.ok(analytics.studentPatterns(courseId));
    }
}
