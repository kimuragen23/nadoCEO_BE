-- 데이터가 적을 때 ivfflat(lists=100)은 빈 결과를 반환할 수 있음
-- 정확한 검색을 위해 ivfflat 인덱스를 제거하고 HNSW로 대체
DROP INDEX IF EXISTS idx_vector_store_embedding;
DROP INDEX IF EXISTS idx_faqs_embedding;

-- HNSW는 데이터 양에 관계없이 정확한 ANN 검색 가능
CREATE INDEX IF NOT EXISTS idx_vector_store_embedding_hnsw
    ON vector_store USING hnsw (embedding vector_cosine_ops);
