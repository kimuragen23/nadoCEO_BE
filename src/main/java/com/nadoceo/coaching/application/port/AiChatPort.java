package com.nadoceo.coaching.application.port;

import reactor.core.publisher.Flux;

import java.util.List;

public interface AiChatPort {

    record ChatMessage(String role, String content) {}

    Flux<String> streamSocratic(String userMessage, List<ChatMessage> history, String additionalContext, int currentTurn);

    Flux<String> streamTermExplanation(String term, List<ChatMessage> history);
}
