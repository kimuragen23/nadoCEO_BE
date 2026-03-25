package com.nadoceo.coaching.application;

import com.nadoceo.coaching.domain.ChatSession;
import com.nadoceo.coaching.domain.ChatSessionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 레거시 피드백 — 단순 세션 해결 처리만. 개별 메시지 피드백은 MessageFeedbackUseCase 사용.
 */
@Service
public class SubmitFeedbackUseCase {

    private final ChatSessionRepository sessionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SubmitFeedbackUseCase(ChatSessionRepository sessionRepository,
                                  ApplicationEventPublisher eventPublisher) {
        this.sessionRepository = sessionRepository;
        this.eventPublisher = eventPublisher;
    }

    public record Command(UUID sessionId, boolean resolved, String summary) {}

    public void execute(Command command) {
        ChatSession session = sessionRepository.findById(command.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + command.sessionId()));

        if (command.resolved() && !session.isResolved()) {
            session.markResolved(command.summary() != null ? command.summary() : "해결됨");
            sessionRepository.save(session);
            session.domainEvents().forEach(eventPublisher::publishEvent);
            session.clearDomainEvents();
        }
    }
}
