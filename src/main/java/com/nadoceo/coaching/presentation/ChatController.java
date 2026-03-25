package com.nadoceo.coaching.presentation;

import com.nadoceo.coaching.application.GetSessionUseCase;
import com.nadoceo.coaching.application.MessageFeedbackUseCase;
import com.nadoceo.coaching.application.ProcessMessageUseCase;
import com.nadoceo.coaching.domain.ChatSession;
import com.nadoceo.coaching.domain.ChatSessionRepository;
import com.nadoceo.coaching.presentation.dto.ChatRequest;
import com.nadoceo.coaching.presentation.dto.MessageFeedbackRequest;
import com.nadoceo.coaching.presentation.dto.SessionSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Coaching", description = "소크라테스식 코칭 채팅 API")
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ProcessMessageUseCase processMessage;
    private final MessageFeedbackUseCase messageFeedback;
    private final GetSessionUseCase getSession;
    private final ChatSessionRepository sessionRepository;

    public ChatController(ProcessMessageUseCase processMessage,
                          MessageFeedbackUseCase messageFeedback,
                          GetSessionUseCase getSession,
                          ChatSessionRepository sessionRepository) {
        this.processMessage = processMessage;
        this.messageFeedback = messageFeedback;
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

    @Operation(summary = "세션 히스토리 목록", description = "학생의 코칭 세션 히스토리를 조회합니다 (메인 채팅만)")
    @GetMapping("/history")
    public ResponseEntity<List<SessionSummary>> getHistory(@RequestParam UUID studentId) {
        var sessions = sessionRepository.findByStudentIdAndChatType(
                studentId, com.nadoceo.coaching.domain.ChatType.MAIN);
        var summaries = sessions.stream().map(SessionSummary::from).toList();
        return ResponseEntity.ok(summaries);
    }

    @Operation(summary = "메시지별 피드백", description = "각 AI 응답에 대해 좋아요/싫어요/해결완료 피드백")
    @PostMapping("/message-feedback")
    public ResponseEntity<Map<String, Object>> messageFeedback(@Valid @RequestBody MessageFeedbackRequest request) {
        var result = messageFeedback.execute(new MessageFeedbackUseCase.Command(
                request.sessionId(), request.messageIndex(), request.feedbackType()));
        return ResponseEntity.ok(Map.of(
                "status", result.status(),
                "faqId", result.faqId() != null ? result.faqId().toString() : ""
        ));
    }
}
