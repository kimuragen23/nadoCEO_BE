package com.nadoceo.knowledge.domain;

import java.util.UUID;

public record FaqSearchResult(
        UUID faqId,
        String question,
        String answer,
        double similarity
) {}
