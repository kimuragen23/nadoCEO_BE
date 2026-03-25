package com.nadoceo.knowledge.infrastructure.embedding;

import com.nadoceo.knowledge.domain.VectorSearchPort;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PgVectorSearchAdapter implements VectorSearchPort {

    private final VectorStore vectorStore;

    public PgVectorSearchAdapter(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void store(String id, String text, Map<String, Object> metadata) {
        Document doc = new Document(id, text, metadata);
        vectorStore.add(List.of(doc));
    }

    @Override
    public List<VectorDocument> search(String query, int topK, double threshold) {
        SearchRequest request = SearchRequest.query(query)
                .withTopK(topK)
                .withSimilarityThreshold(threshold);

        return vectorStore.similaritySearch(request)
                .stream()
                .map(doc -> new VectorDocument(doc.getId(), doc.getContent(), doc.getMetadata()))
                .toList();
    }
}
