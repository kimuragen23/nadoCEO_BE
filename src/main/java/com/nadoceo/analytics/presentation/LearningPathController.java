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
    private final com.nadoceo.coaching.infrastructure.persistence.SpringDataChatSessionRepo sessionRepo;

    public LearningPathController(GetTimelineUseCase getTimeline,
                                   SpringDataLearningPathRepo lpRepo,
                                   com.nadoceo.coaching.infrastructure.persistence.SpringDataChatSessionRepo sessionRepo) {
        this.getTimeline = getTimeline;
        this.lpRepo = lpRepo;
        this.sessionRepo = sessionRepo;
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

    @Operation(summary = "용어 상세 조회", description = "해당 용어를 검색했던 세션의 Q&A를 반환")
    @GetMapping("/glossary/detail")
    public ResponseEntity<Map<String, Object>> getTermDetail(@RequestParam UUID studentId, @RequestParam String term) {
        var sessionIdOpt = lpRepo.findSessionByTerm(studentId, term);
        if (sessionIdOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("term", term, "answer", "상세 내용이 없습니다."));
        }

        var sessionOpt = sessionRepo.findById(sessionIdOpt.get());
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("term", term, "answer", "세션을 찾을 수 없습니다."));
        }

        // 세션 messages에서 해당 용어 Q&A 추출
        try {
            String json = sessionOpt.get().getMessages();
            var messages = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<Map<String, String>>>() {});

            // 용어와 일치하는 user 메시지 바로 다음의 assistant 메시지를 찾기
            for (int i = 0; i < messages.size() - 1; i++) {
                var msg = messages.get(i);
                if ("user".equals(msg.get("role")) && msg.get("content") != null
                        && msg.get("content").contains(term)) {
                    var nextMsg = messages.get(i + 1);
                    if ("assistant".equals(nextMsg.get("role"))) {
                        return ResponseEntity.ok(Map.of("term", term, "answer", nextMsg.get("content")));
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }

        return ResponseEntity.ok(Map.of("term", term, "answer", "상세 내용이 없습니다."));
    }
}
