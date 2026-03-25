package com.nadoceo.knowledge.domain;

import java.util.List;
import java.util.Map;

public interface VectorSearchPort {

    record VectorDocument(String id, String content, Map<String, Object> metadata) {}

    void store(String id, String text, Map<String, Object> metadata);

    List<VectorDocument> search(String query, int topK, double threshold);
}
