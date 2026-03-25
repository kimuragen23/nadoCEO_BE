package com.nadoceo.knowledge.infrastructure;

import com.nadoceo.coaching.application.port.KnowledgeSearchPort;
import com.nadoceo.knowledge.application.SearchFaqUseCase;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * coaching 컨텍스트의 KnowledgeSearchPort를 구현하여
 * knowledge 컨텍스트의 SearchFaqUseCase에 위임한다.
 * 이를 통해 coaching → knowledge 간 의존성 역전을 달성한다.
 */
@Component
public class KnowledgeSearchPortAdapter implements KnowledgeSearchPort {

    private final SearchFaqUseCase searchFaq;

    public KnowledgeSearchPortAdapter(SearchFaqUseCase searchFaq) {
        this.searchFaq = searchFaq;
    }

    @Override
    public Optional<SearchResult> search(String query, UUID courseId) {
        return searchFaq.execute(query, courseId)
                .map(faq -> new SearchResult(faq.faqId(), faq.question(), faq.answer(), faq.similarity()));
    }
}
