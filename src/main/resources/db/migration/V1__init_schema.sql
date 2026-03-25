-- pgvector 확장
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 학원
CREATE TABLE academies (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 과목
CREATE TABLE courses (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id  UUID NOT NULL REFERENCES academies(id),
    name        VARCHAR(255) NOT NULL,
    description TEXT
);

-- 사용자
CREATE TABLE users (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    academy_id UUID NOT NULL REFERENCES academies(id),
    role       VARCHAR(20) NOT NULL,  -- student | instructor | admin
    name       VARCHAR(255) NOT NULL
);

-- FAQ (벡터 저장소)
CREATE TABLE faqs (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    course_id  UUID NOT NULL REFERENCES courses(id),
    question   TEXT NOT NULL,
    answer     TEXT NOT NULL,
    embedding  vector(1536),
    upvotes    INTEGER DEFAULT 0,
    source     VARCHAR(20) NOT NULL DEFAULT 'student',  -- student | instructor
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- pgvector 인덱스
CREATE INDEX idx_faqs_embedding ON faqs USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
CREATE INDEX idx_faqs_course_id ON faqs (course_id);

-- 채팅 세션
CREATE TABLE chat_sessions (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id  UUID NOT NULL REFERENCES users(id),
    course_id   UUID NOT NULL REFERENCES courses(id),
    chat_type   VARCHAR(10) NOT NULL DEFAULT 'main',  -- main | sub
    messages    JSONB DEFAULT '[]'::jsonb,
    total_turns INTEGER DEFAULT 0,
    resolved    BOOLEAN DEFAULT false,
    resolved_at TIMESTAMP,
    faq_id      UUID REFERENCES faqs(id),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_sessions_student ON chat_sessions (student_id);
CREATE INDEX idx_chat_sessions_course  ON chat_sessions (course_id);

-- 학습 경로 이벤트
CREATE TABLE learning_path_events (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id  UUID NOT NULL REFERENCES chat_sessions(id),
    student_id  UUID NOT NULL REFERENCES users(id),
    course_id   UUID NOT NULL REFERENCES courses(id),
    event_type  VARCHAR(30) NOT NULL,
    turn_number INTEGER NOT NULL,
    content     TEXT,
    faq_hit_id  UUID REFERENCES faqs(id),
    metadata    JSONB,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lpe_session  ON learning_path_events (session_id);
CREATE INDEX idx_lpe_course   ON learning_path_events (course_id);
CREATE INDEX idx_lpe_student  ON learning_path_events (student_id);
CREATE INDEX idx_lpe_type     ON learning_path_events (event_type);
