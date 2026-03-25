package com.nadoceo.coaching.presentation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MessageFeedbackRequest(
        @NotNull UUID sessionId,
        int messageIndex,             // 몇 번째 메시지에 대한 피드백인지 (0-based)
        @NotNull String feedbackType  // "up" | "down" | "resolved"
) {}
