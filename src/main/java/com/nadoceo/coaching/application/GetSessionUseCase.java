package com.nadoceo.coaching.application;

import com.nadoceo.coaching.domain.ChatSession;
import com.nadoceo.coaching.domain.ChatSessionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetSessionUseCase {

    private final ChatSessionRepository sessionRepository;

    public GetSessionUseCase(ChatSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public ChatSession execute(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }
}
