package com.nadoceo.coaching.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChatRequest(
        UUID sessionId,
        @NotBlank String message,
        @NotNull UUID courseId,
        @NotNull UUID studentId,
        String chatType
) {}
