package com.nadoceo.knowledge.application;

import com.nadoceo.knowledge.domain.FaqSearchResult;
import com.nadoceo.knowledge.domain.VectorSearchPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class SearchFaqUseCase {

    private final VectorSearchPort vectorSearch;

    @Value("${nadoceo.coaching.faq-similarity-threshold:0.85}")
    private double similarityThreshold;

    public SearchFaqUseCase(VectorSearchPort vectorSearch) {
        this.vectorSearch = vectorSearch;
    }

    public Optional<FaqSearchResult> execute(String query, UUID courseId) {
        return vectorSearch.search(query, 5, similarityThreshold)
                .stream()
                .filter(doc -> courseId.toString().equals(doc.metadata().get("courseId")))
                .findFirst()
                .map(doc -> new FaqSearchResult(
                        UUID.fromString(doc.metadata().get("faqId").toString()),
                        doc.metadata().get("question").toString(),
                        doc.metadata().get("answer").toString(),
                        doc.metadata().containsKey("distance")
                                ? 1.0 - ((Number) doc.metadata().get("distance")).doubleValue()
                                : similarityThreshold
                ));
    }
}
