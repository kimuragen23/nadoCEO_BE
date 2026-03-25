package com.nadoceo.coaching.application;

import com.nadoceo.coaching.domain.ChatSession;
import com.nadoceo.coaching.domain.ChatSessionRepository;
import com.nadoceo.shared.domain.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

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

        if (command.resolved()) {
            session.markResolved(command.summary() != null ? command.summary() : "해결됨");
            sessionRepository.save(session);

            // 도메인 이벤트 발행
            session.domainEvents().forEach(eventPublisher::publishEvent);
            session.clearDomainEvents();
        }
    }
}
