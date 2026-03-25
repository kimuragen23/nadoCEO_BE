package com.nadoceo.coaching.application;

import com.nadoceo.coaching.application.port.AiChatPort;
import com.nadoceo.coaching.application.port.KnowledgeSearchPort;
import com.nadoceo.coaching.domain.*;
import com.nadoceo.coaching.domain.event.FaqSearchRequested;
import com.nadoceo.coaching.domain.event.SocraticQuestionDelivered;
import com.nadoceo.coaching.domain.event.TermLookupRequested;
import com.nadoceo.shared.domain.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProcessMessageUseCase {

    private final ChatSessionRepository sessionRepository;
    private final CoachingEngine coachingEngine;
    private final AiChatPort aiChat;
    private final KnowledgeSearchPort knowledgeSearch;
    private final ApplicationEventPublisher eventPublisher;

    public ProcessMessageUseCase(ChatSessionRepository sessionRepository,
                                  AiChatPort aiChat,
                                  KnowledgeSearchPort knowledgeSearch,
                                  ApplicationEventPublisher eventPublisher) {
        this.sessionRepository = sessionRepository;
        this.coachingEngine = new CoachingEngine();
        this.aiChat = aiChat;
        this.knowledgeSearch = knowledgeSearch;
        this.eventPublisher = eventPublisher;
    }

    public record Command(UUID sessionId, String message, UUID courseId, UUID studentId, String chatType) {}

    public Flux<String> execute(Command command) {
        ChatType chatType = ChatType.from(command.chatType());
        ChatSession session = findOrCreate(command, chatType);

        // 도메인 로직: 턴 진행 + 이벤트 등록
        session.recordQuestion(command.message());

        // 도메인 서비스: 코칭 상태 판단
        CoachingState state = coachingEngine.determine(
                command.message(), session.getChatType(), session.getTotalTurns());

        // 세션 저장
        sessionRepository.save(session);

        // 도메인 이벤트 발행
        publishEvents(session);

        // 상태별 AI 스트리밍 처리
        return switch (state) {
            case SOCRATIC -> handleSocratic(command, session);
            case FAQ_SEARCH -> handleFaqSearch(command, session);
            case CONTINUE -> aiChat.streamSocratic(command.message(), null);
            case TERM_LOOKUP -> handleTermLookup(command, session);
        };
    }

    private Flux<String> handleSocratic(Command command, ChatSession session) {
        return aiChat.streamSocratic(command.message(), null)
                .doOnComplete(() -> eventPublisher.publishEvent(
                        new SocraticQuestionDelivered(
                                session.getId(), session.getStudentId(),
                                session.getCourseId(), session.getTotalTurns())
                ));
    }

    private Flux<String> handleFaqSearch(Command command, ChatSession session) {
        Optional<KnowledgeSearchPort.SearchResult> result =
                knowledgeSearch.search(command.message(), command.courseId());

        if (result.isPresent()) {
            var faq = result.get();
            eventPublisher.publishEvent(FaqSearchRequested.hit(
                    session.getId(), session.getStudentId(), session.getCourseId(),
                    session.getTotalTurns(), command.message(), faq.faqId(), faq.similarity()));

            String context = "다음 FAQ를 참고하여 학생에게 친절하게 설명하세요:\n질문: %s\n답변: %s"
                    .formatted(faq.question(), faq.answer());
            return aiChat.streamSocratic(command.message(), context);
        } else {
            eventPublisher.publishEvent(FaqSearchRequested.miss(
                    session.getId(), session.getStudentId(), session.getCourseId(),
                    session.getTotalTurns(), command.message()));

            return aiChat.streamSocratic(command.message(), null);
        }
    }

    private Flux<String> handleTermLookup(Command command, ChatSession session) {
        eventPublisher.publishEvent(new TermLookupRequested(
                session.getId(), session.getStudentId(), session.getCourseId(),
                session.getTotalTurns(), command.message()));

        return aiChat.streamTermExplanation(command.message());
    }

    private void publishEvents(ChatSession session) {
        session.domainEvents().forEach(eventPublisher::publishEvent);
        session.clearDomainEvents();
    }

    private ChatSession findOrCreate(Command command, ChatType chatType) {
        if (command.sessionId() != null) {
            return sessionRepository.findById(command.sessionId())
                    .orElseGet(() -> createSession(command, chatType));
        }
        return createSession(command, chatType);
    }

    private ChatSession createSession(Command command, ChatType chatType) {
        ChatSession session = new ChatSession(command.studentId(), command.courseId(), chatType);
        return sessionRepository.save(session);
    }
}
