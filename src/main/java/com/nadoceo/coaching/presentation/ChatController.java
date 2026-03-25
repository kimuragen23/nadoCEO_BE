package com.nadoceo.coaching.presentation;

import com.nadoceo.coaching.application.GetSessionUseCase;
import com.nadoceo.coaching.application.ProcessMessageUseCase;
import com.nadoceo.coaching.application.SubmitFeedbackUseCase;
import com.nadoceo.coaching.domain.ChatSession;
import com.nadoceo.coaching.domain.ChatSessionRepository;
import com.nadoceo.coaching.presentation.dto.ChatRequest;
import com.nadoceo.coaching.presentation.dto.FeedbackRequest;
import com.nadoceo.coaching.presentation.dto.SessionSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Tag(name = "Coaching", description = "소크라테스식 코칭 채팅 API")
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ProcessMessageUseCase processMessage;
    private final SubmitFeedbackUseCase submitFeedback;
    private final GetSessionUseCase getSession;
    private final ChatSessionRepository sessionRepository;

    public ChatController(ProcessMessageUseCase processMessage,
                          SubmitFeedbackUseCase submitFeedback,
                          GetSessionUseCase getSession,
                          ChatSessionRepository sessionRepository) {
        this.processMessage = processMessage;
        this.submitFeedback = submitFeedback;
        this.getSession = getSession;
        this.sessionRepository = sessionRepository;
    }

    @Operation(summary = "메시지 전송", description = "코칭 메시지를 전송하고 SSE 스트리밍으로 AI 응답을 받습니다")
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessage(@Valid @RequestBody ChatRequest request) {
        return processMessage.execute(new ProcessMessageUseCase.Command(
                request.sessionId(), request.message(),
                request.courseId(), request.studentId(), request.chatType()));
    }

    @Operation(summary = "세션 조회")
    @GetMapping("/{sessionId}")
    public ResponseEntity<ChatSession> getSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(getSession.execute(sessionId));
    }

    @Operation(summary = "세션 히스토리 목록", description = "학생의 코칭 세션 히스토리를 조회합니다")
    @GetMapping("/history")
    public ResponseEntity<List<SessionSummary>> getHistory(@RequestParam UUID studentId) {
        var sessions = sessionRepository.findByStudentId(studentId);
        var summaries = sessions.stream().map(SessionSummary::from).toList();
        return ResponseEntity.ok(summaries);
    }

    @Operation(summary = "피드백 제출", description = "세션 해결/미해결 피드백을 제출합니다")
    @PostMapping("/feedback")
    public ResponseEntity<Void> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        submitFeedback.execute(new SubmitFeedbackUseCase.Command(
                request.sessionId(), request.resolved(), request.summary()));
        return ResponseEntity.ok().build();
    }
}
