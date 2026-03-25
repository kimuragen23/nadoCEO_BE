package com.nadoceo.coaching.presentation.dto;

import java.util.UUID;

public sealed interface ChatResponse {

    record Streaming(String content, UUID sessionId, int turnNumber) implements ChatResponse {}

    record FaqHit(UUID faqId, String question, String answer, double similarity, UUID sessionId) implements ChatResponse {}

    record Error(String message) implements ChatResponse {}

    record SessionCreated(UUID sessionId) implements ChatResponse {}
}
