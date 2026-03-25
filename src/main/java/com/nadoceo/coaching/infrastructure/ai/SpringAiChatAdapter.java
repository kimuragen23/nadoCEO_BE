package com.nadoceo.coaching.infrastructure.ai;

import com.nadoceo.coaching.application.port.AiChatPort;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class SpringAiChatAdapter implements AiChatPort {

    private final ChatClient chatClient;

    private static final String SOCRATIC_PROMPT = """
            당신은 IT 교육 코칭 AI입니다. 학생이 스스로 문제를 해결할 수 있도록 소크라테스식 질문법을 사용합니다.

            규칙:
            1. 즉시 정답을 알려주지 마세요.
            2. 학생이 스스로 생각할 수 있는 질문을 던지세요.
            3. 에러 메시지가 있다면, 학생에게 에러 메시지의 의미를 물어보세요.
            4. 최대 2번의 역질문 후에는 구체적인 힌트를 제공하세요.
            5. 친절하고 격려하는 톤을 유지하세요.
            6. 한국어로 답변하세요.
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
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public Flux<String> streamSocratic(String userMessage, String additionalContext) {
        String systemPrompt = additionalContext != null
                ? SOCRATIC_PROMPT + "\n\n" + additionalContext
                : SOCRATIC_PROMPT;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .stream()
                .content();
    }

    @Override
    public Flux<String> streamTermExplanation(String term) {
        return chatClient.prompt()
                .system(TERM_PROMPT)
                .user(term)
                .stream()
                .content();
    }
}
