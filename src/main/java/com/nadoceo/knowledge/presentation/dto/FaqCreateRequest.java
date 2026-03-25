package com.nadoceo.knowledge.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FaqCreateRequest(
        @NotNull UUID courseId,
        @NotBlank String question,
        @NotBlank String answer,
        String source
) {}
