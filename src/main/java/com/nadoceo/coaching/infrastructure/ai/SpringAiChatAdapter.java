package com.nadoceo.coaching.infrastructure.ai;

import com.nadoceo.coaching.application.port.AiChatPort;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpringAiChatAdapter implements AiChatPort {

    private final ChatModel chatModel;

    private static final String SOCRATIC_BASE = """
            당신은 IT 교육 코칭 AI입니다. 학생이 스스로 문제를 해결할 수 있도록 소크라테스식 질문법을 사용합니다.

            규칙:
            1. 이전 대화 내용을 반드시 참고하세요.
            2. 친절하고 격려하는 톤을 유지하세요.
            3. 한국어로 답변하세요.
            """;

    private static final String PHASE_QUESTION = """
            현재 코칭 단계: 질문 단계 (턴 %d)
            - 즉시 정답을 알려주지 마세요.
            - 학생이 스스로 생각할 수 있는 질문을 던지세요.
            - 에러 메시지가 있다면, 학생에게 에러 메시지의 의미를 물어보세요.
            """;

    private static final String PHASE_HINT = """
            현재 코칭 단계: 힌트 단계 (턴 %d)
            - 학생이 충분히 생각해봤으므로 이제 구체적인 힌트를 제공하세요.
            - 핵심 개념이나 해결 방향을 알려주되, 완전한 코드는 아직 주지 마세요.
            - 학생이 힌트를 바탕으로 스스로 시도할 수 있게 유도하세요.
            """;

    private static final String PHASE_ANSWER = """
            현재 코칭 단계: 답변 단계 (턴 %d)
            - 이제 명확하고 구체적인 답변을 제공하세요.
            - 코드 예시가 필요하면 포함하세요.
            - 왜 이 해결법이 맞는지 설명하세요.
            - 학생이 이해할 수 있도록 단계별로 설명하세요.
            """;

    private static final String TERM_PROMPT = """
            당신은 IT 교육 도우미입니다. 학생이 질문한 용어나 개념을 쉽고 명확하게 설명합니다.

            규칙:
            1. 간결하면서도 정확하게 설명하세요.
            2. 가능하면 예시를 들어 설명하세요.
            3. 관련 개념도 간략히 언급하세요.
            4. 한국어로 답변하세요.
            """;

    public SpringAiChatAdapter(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Flux<String> streamSocratic(String userMessage, List<ChatMessage> history,
                                        String additionalContext, int currentTurn) {
        // 턴 수에 따라 코칭 단계 전환
        String phasePrompt;
        if (currentTurn <= 2) {
            phasePrompt = PHASE_QUESTION.formatted(currentTurn);
        } else if (currentTurn <= 4) {
            phasePrompt = PHASE_HINT.formatted(currentTurn);
        } else {
            phasePrompt = PHASE_ANSWER.formatted(currentTurn);
        }

        String systemPrompt = SOCRATIC_BASE + "\n" + phasePrompt;
        if (additionalContext != null) {
            systemPrompt += "\n\n" + additionalContext;
        }

        List<Message> messages = buildMessages(systemPrompt, history, userMessage);
        return chatModel.stream(new Prompt(messages))
                .map(response -> {
                    if (response.getResult() != null && response.getResult().getOutput() != null) {
                        String text = response.getResult().getOutput().getContent();
                        return text != null ? text : "";
                    }
                    return "";
                })
                .filter(s -> !s.isEmpty());
    }

    @Override
    public Flux<String> streamTermExplanation(String term, List<ChatMessage> history) {
        List<Message> messages = buildMessages(TERM_PROMPT, history, term);
        return chatModel.stream(new Prompt(messages))
                .map(response -> {
                    if (response.getResult() != null && response.getResult().getOutput() != null) {
                        String text = response.getResult().getOutput().getContent();
                        return text != null ? text : "";
                    }
                    return "";
                })
                .filter(s -> !s.isEmpty());
    }

    private List<Message> buildMessages(String systemPrompt, List<ChatMessage> history, String currentMessage) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));

        if (history != null) {
            for (ChatMessage msg : history) {
                if ("user".equals(msg.role())) {
                    messages.add(new UserMessage(msg.content()));
                } else if ("assistant".equals(msg.role())) {
                    messages.add(new AssistantMessage(msg.content()));
                }
            }
        }

        messages.add(new UserMessage(currentMessage));
        return messages;
    }
}
