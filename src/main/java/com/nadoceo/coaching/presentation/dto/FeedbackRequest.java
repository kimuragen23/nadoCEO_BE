package com.nadoceo.coaching.presentation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FeedbackRequest(
        @NotNull UUID sessionId,
        boolean resolved,
        String summary
) {}
