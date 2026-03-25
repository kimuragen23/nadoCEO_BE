package com.nadoceo.coaching.application;

import com.nadoceo.coaching.domain.ChatSession;
import com.nadoceo.coaching.domain.ChatSessionRepository;
import com.nadoceo.knowledge.application.CreateFaqUseCase;
import com.nadoceo.knowledge.domain.FaqRepository;
import com.nadoceo.knowledge.domain.FaqSearchResult;
import com.nadoceo.knowledge.application.SearchFaqUseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MessageFeedbackUseCase {

    private final ChatSessionRepository sessionRepository;
    private final CreateFaqUseCase createFaq;
    private final SearchFaqUseCase searchFaq;
    private final FaqRepository faqRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageFeedbackUseCase(ChatSessionRepository sessionRepository,
                                   CreateFaqUseCase createFaq,
                                   SearchFaqUseCase searchFaq,
                                   FaqRepository faqRepository,
                                   ApplicationEventPublisher eventPublisher) {
        this.sessionRepository = sessionRepository;
        this.createFaq = createFaq;
        this.searchFaq = searchFaq;
        this.faqRepository = faqRepository;
        this.eventPublisher = eventPublisher;
    }

    public record Command(UUID sessionId, int messageIndex, String feedbackType) {}

    public record Result(String status, UUID faqId) {}

    public Result execute(Command command) {
        ChatSession session = sessionRepository.findById(command.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + command.sessionId()));

        // 메시지 히스토리에서 해당 AI 응답과 직전 사용자 질문을 찾는다
        QaPair qa = extractQaPair(session, command.messageIndex());
        if (qa == null) {
            return new Result("no_message", null);
        }

        return switch (command.feedbackType()) {
            case "up" -> handleUpvote(session, qa);
            case "down" -> new Result("noted", null);
            case "resolved" -> handleResolved(session, qa);
            default -> new Result("unknown_type", null);
        };
    }

    /**
     * 좋아요: 해당 Q&A를 FAQ로 저장하거나, 이미 있으면 upvote 증가
     */
    private Result handleUpvote(ChatSession session, QaPair qa) {
        // 기존 유사 FAQ가 있는지 검색
        Optional<FaqSearchResult> existing = searchFaq.execute(qa.question(), session.getCourseId());

        if (existing.isPresent() && existing.get().similarity() > 0.90) {
            // 유사도 높은 기존 FAQ에 upvote
            var faq = faqRepository.findById(existing.get().faqId()).orElse(null);
            if (faq != null) {
                faq.upvote();
                faqRepository.save(faq);
                return new Result("upvoted", faq.getId());
            }
        }

        // 새 FAQ 생성
        var faq = createFaq.execute(new CreateFaqUseCase.Command(
                session.getCourseId(), qa.question(), qa.answer(), "student"));
        return new Result("created", faq.getId());
    }

    /**
     * 해결완료: FAQ 저장 + 세션 해결 처리
     */
    private Result handleResolved(ChatSession session, QaPair qa) {
        // FAQ 저장 (좋아요와 동일)
        Result faqResult = handleUpvote(session, qa);

        // 세션 해결 처리
        if (!session.isResolved()) {
            session.markResolved(qa.question());
            sessionRepository.save(session);
            session.domainEvents().forEach(eventPublisher::publishEvent);
            session.clearDomainEvents();
        }

        return new Result("resolved", faqResult.faqId());
    }

    /**
     * messageIndex 위치의 AI 응답과 그 직전 사용자 질문을 추출한다.
     */
    private QaPair extractQaPair(ChatSession session, int messageIndex) {
        try {
            String json = session.getMessages();
            if (json == null || json.equals("[]")) return null;

            List<Map<String, String>> messages = objectMapper.readValue(json, new TypeReference<>() {});
            if (messageIndex < 0 || messageIndex >= messages.size()) return null;

            Map<String, String> targetMsg = messages.get(messageIndex);
            if (!"assistant".equals(targetMsg.get("role"))) return null;

            String answer = targetMsg.get("content");

            // 직전 사용자 메시지를 찾는다
            String question = null;
            for (int i = messageIndex - 1; i >= 0; i--) {
                if ("user".equals(messages.get(i).get("role"))) {
                    question = messages.get(i).get("content");
                    break;
                }
            }

            if (question == null || answer == null) return null;
            return new QaPair(question, answer);
        } catch (Exception e) {
            return null;
        }
    }

    private record QaPair(String question, String answer) {}
}
