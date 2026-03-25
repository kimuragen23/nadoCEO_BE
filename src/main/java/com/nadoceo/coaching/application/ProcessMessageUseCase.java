package com.nadoceo.coaching.application;

import com.nadoceo.coaching.application.port.AiChatPort;
import com.nadoceo.coaching.application.port.KnowledgeSearchPort;
import com.nadoceo.coaching.domain.*;
import com.nadoceo.coaching.domain.event.FaqSearchRequested;
import com.nadoceo.coaching.domain.event.SocraticQuestionDelivered;
import com.nadoceo.coaching.domain.event.TermLookupRequested;
import com.nadoceo.identity.domain.User;
import com.nadoceo.identity.domain.UserRole;
import com.nadoceo.identity.infrastructure.persistence.SpringDataUserRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
public class ProcessMessageUseCase {

    private static final UUID DEFAULT_ACADEMY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final ChatSessionRepository sessionRepository;
    private final CoachingEngine coachingEngine;
    private final AiChatPort aiChat;
    private final KnowledgeSearchPort knowledgeSearch;
    private final ApplicationEventPublisher eventPublisher;
    private final SpringDataUserRepo userRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProcessMessageUseCase(ChatSessionRepository sessionRepository,
                                  AiChatPort aiChat,
                                  KnowledgeSearchPort knowledgeSearch,
                                  ApplicationEventPublisher eventPublisher,
                                  SpringDataUserRepo userRepo) {
        this.sessionRepository = sessionRepository;
        this.coachingEngine = new CoachingEngine();
        this.aiChat = aiChat;
        this.knowledgeSearch = knowledgeSearch;
        this.eventPublisher = eventPublisher;
        this.userRepo = userRepo;
    }

    public record Command(UUID sessionId, String message, UUID courseId, UUID studentId, String chatType) {}

    public Flux<String> execute(Command command) {
        ensureUserExists(command.studentId());

        ChatType chatType = ChatType.from(command.chatType());
        ChatSession session = findOrCreate(command, chatType);

        // 이전 대화 히스토리 로드
        List<AiChatPort.ChatMessage> history = loadHistory(session);

        // 도메인 로직: 턴 진행 + 유저 메시지 저장
        session.recordQuestion(command.message());
        session.appendMessage("user", command.message());

        // 코칭 상태 판단
        CoachingState state = coachingEngine.determine(
                command.message(), session.getChatType(), session.getTotalTurns());

        // 세션 저장
        sessionRepository.save(session);
        publishEvents(session);

        // 매 질문마다 FAQ 검색 (TERM_LOOKUP 제외)
        String faqContext = null;
        String faqHitEvent = null;
        if (state != CoachingState.TERM_LOOKUP) {
            try {
                var faqResult = searchFaqForStream(command, session);
                faqContext = faqResult.context();
                faqHitEvent = faqResult.sseEvent();
            } catch (Exception e) {
                // FAQ 검색 실패해도 AI 응답은 진행
            }
        }

        // sessionId를 첫 번째 SSE 이벤트로 전달
        String sessionIdEvent = "%%SESSION_ID%%" + session.getId().toString();
        final String finalFaqContext = faqContext;
        final String finalFaqHitEvent = faqHitEvent;
        final ChatSession finalSession = session;

        final int turnCount = session.getTotalTurns();
        Flux<String> aiStream = switch (state) {
            case SOCRATIC, FAQ_SEARCH, CONTINUE ->
                    aiChat.streamSocratic(command.message(), history, finalFaqContext, turnCount)
                            .doOnComplete(() -> eventPublisher.publishEvent(
                                    new SocraticQuestionDelivered(
                                            finalSession.getId(), finalSession.getStudentId(),
                                            finalSession.getCourseId(), finalSession.getTotalTurns())));
            case TERM_LOOKUP -> {
                eventPublisher.publishEvent(new TermLookupRequested(
                        session.getId(), session.getStudentId(), session.getCourseId(),
                        session.getTotalTurns(), command.message()));
                yield aiChat.streamTermExplanation(command.message(), history);
            }
        };

        // AI 응답을 수집해서 세션에 저장
        StringBuilder responseBuffer = new StringBuilder();
        // 메타 이벤트: sessionId + FAQ 히트 정보
        List<String> metaEvents = new java.util.ArrayList<>();
        metaEvents.add(sessionIdEvent);
        if (finalFaqHitEvent != null) metaEvents.add(finalFaqHitEvent);

        return Flux.concat(
                Flux.fromIterable(metaEvents),
                aiStream.doOnNext(responseBuffer::append)
                        .doOnComplete(() -> {
                            finalSession.appendMessage("assistant", responseBuffer.toString());
                            sessionRepository.save(finalSession);
                        })
        );
    }

    private record FaqSearchOutput(String context, String sseEvent) {}

    private FaqSearchOutput searchFaqForStream(Command command, ChatSession session) {
        Optional<KnowledgeSearchPort.SearchResult> result =
                knowledgeSearch.search(command.message(), command.courseId());

        if (result.isPresent()) {
            var faq = result.get();
            eventPublisher.publishEvent(FaqSearchRequested.hit(
                    session.getId(), session.getStudentId(), session.getCourseId(),
                    session.getTotalTurns(), command.message(), faq.faqId(), faq.similarity()));

            String context = "관련 FAQ가 있습니다:\n질문: %s\n답변: %s\n(유사도: %.0f%%)\n이 정보를 참고하여 코칭하세요."
                    .formatted(faq.question(), faq.answer(), faq.similarity() * 100);

            // FAQ 히트 정보를 SSE 이벤트로 프론트에 전달 (질문 + 답변)
            String sseEvent = "%%FAQ_HIT%%{\"faqId\":\"%s\",\"similarity\":%.2f,\"question\":\"%s\",\"answer\":\"%s\"}"
                    .formatted(faq.faqId(), faq.similarity(),
                            faq.question().replace("\"", "\\\"").replace("\n", " "),
                            faq.answer().replace("\"", "\\\"").replace("\n", " "));

            return new FaqSearchOutput(context, sseEvent);
        } else {
            eventPublisher.publishEvent(FaqSearchRequested.miss(
                    session.getId(), session.getStudentId(), session.getCourseId(),
                    session.getTotalTurns(), command.message()));
            return new FaqSearchOutput(null, null);
        }
    }

    private List<AiChatPort.ChatMessage> loadHistory(ChatSession session) {
        try {
            String json = session.getMessages();
            if (json == null || json.equals("[]")) return List.of();

            List<Map<String, String>> raw = objectMapper.readValue(json, new TypeReference<>() {});
            return raw.stream()
                    .map(m -> new AiChatPort.ChatMessage(m.get("role"), m.get("content")))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private void ensureUserExists(UUID userId) {
        if (!userRepo.existsById(userId)) {
            userRepo.save(new User(userId, DEFAULT_ACADEMY_ID, UserRole.STUDENT, "사용자"));
        }
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
        return sessionRepository.save(new ChatSession(command.studentId(), command.courseId(), chatType));
    }
}
