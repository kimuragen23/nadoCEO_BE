package com.nadoceo.coaching.application.port;

import reactor.core.publisher.Flux;

/**
 * AI 채팅 Output Port.
 * 코칭 컨텍스트가 AI 프로바이더에 의존하지 않도록 추상화.
 */
public interface AiChatPort {

    Flux<String> streamSocratic(String userMessage, String additionalContext);

    Flux<String> streamTermExplanation(String term);
}
