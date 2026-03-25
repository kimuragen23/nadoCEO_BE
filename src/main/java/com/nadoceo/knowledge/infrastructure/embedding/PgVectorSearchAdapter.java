package com.nadoceo.knowledge.infrastructure.embedding;

import com.nadoceo.knowledge.domain.VectorSearchPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PgVectorSearchAdapter implements VectorSearchPort {

    private static final Logger log = LoggerFactory.getLogger(PgVectorSearchAdapter.class);

    private final VectorStore vectorStore;

    public PgVectorSearchAdapter(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void store(String id, String text, Map<String, Object> metadata) {
        Document doc = new Document(id, text, metadata);
        vectorStore.add(List.of(doc));
        log.info("Stored vector: id={}, text={}", id, text.substring(0, Math.min(50, text.length())));
    }

    @Override
    public List<VectorDocument> search(String query, int topK, double threshold) {
        try {
            SearchRequest request = SearchRequest.query(query)
                    .withTopK(topK)
                    .withSimilarityThreshold(threshold);

            List<Document> results = vectorStore.similaritySearch(request);
            log.info("Vector search query='{}' threshold={} results={}", query, threshold, results.size());

            return results.stream()
                    .map(doc -> new VectorDocument(doc.getId(), doc.getContent(), doc.getMetadata()))
                    .toList();
        } catch (Exception e) {
            log.error("Vector search failed for query='{}': {}", query, e.getMessage());
            return List.of();
        }
    }
}
