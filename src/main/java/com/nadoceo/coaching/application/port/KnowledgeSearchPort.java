package com.nadoceo.coaching.application.port;

import java.util.Optional;
import java.util.UUID;

/**
 * FAQ 검색 Output Port.
 * 코칭 컨텍스트가 knowledge 컨텍스트에 직접 의존하지 않도록 추상화.
 */
public interface KnowledgeSearchPort {

    record SearchResult(UUID faqId, String question, String answer, double similarity) {}

    Optional<SearchResult> search(String query, UUID courseId);
}
