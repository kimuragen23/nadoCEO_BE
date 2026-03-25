# NADOCEO 시스템 시퀀스 다이어그램

> 전체 시스템의 핵심 플로우를 Mermaid 시퀀스 다이어그램으로 정리합니다.

---

## 1. 소크라테스식 코칭 플로우 (Main Chat)

학생이 질문을 보내면 CoachingEngine이 메시지를 분석하여 코칭 상태를 결정하고, 상태에 따라 다른 처리 경로를 탑니다.

```mermaid
sequenceDiagram
    actor Student as 학생 (Frontend)
    participant Controller as ChatController
    participant UseCase as ProcessMessageUseCase
    participant Engine as CoachingEngine
    participant Session as ChatSession
    participant AI as SpringAiChatAdapter
    participant GPT as OpenAI GPT-4o
    participant Publisher as EventPublisher
    participant Listener as CoachingEventListener
    participant Record as RecordEventUseCase
    participant DB as PostgreSQL

    Student->>Controller: POST /api/v1/chat (SSE)
    Controller->>UseCase: execute(Command)

    UseCase->>Session: findOrCreate(sessionId)
    Session-->>UseCase: ChatSession

    UseCase->>Session: recordQuestion(message)
    Note over Session: advanceTurn() + <br/>registerEvent(StudentQuestionAsked)

    UseCase->>Engine: determine(message, chatType, turn)
    Engine-->>UseCase: CoachingState

    UseCase->>DB: sessionRepository.save(session)
    UseCase->>Publisher: publishEvent(StudentQuestionAsked)
    Publisher-->>Listener: @Async @EventListener
    Listener->>Record: execute(STUDENT_QUESTION, ...)
    Record->>DB: learning_path_events INSERT

    alt CoachingState = SOCRATIC
        UseCase->>AI: streamSocratic(message, null)
        AI->>GPT: ChatClient.stream()
        GPT-->>AI: SSE chunks
        AI-->>Controller: Flux<String>
        Controller-->>Student: text/event-stream (실시간)
        Note over UseCase: onComplete →
        UseCase->>Publisher: publishEvent(SocraticQuestionDelivered)
    else CoachingState = FAQ_SEARCH
        ref over UseCase: FAQ 검색 플로우 (아래 참조)
    else CoachingState = CONTINUE
        UseCase->>AI: streamSocratic(message, null)
        AI-->>Controller: Flux<String>
        Controller-->>Student: text/event-stream
    else CoachingState = TERM_LOOKUP
        ref over UseCase: 용어 검색 플로우 (아래 참조)
    end
```

---

## 2. FAQ 벡터 검색 플로우

CoachingState가 FAQ_SEARCH일 때, pgvector에서 유사도 검색을 수행하고 히트/미스에 따라 분기합니다.

```mermaid
sequenceDiagram
    actor Student as 학생 (Frontend)
    participant UseCase as ProcessMessageUseCase
    participant Port as KnowledgeSearchPort
    participant Adapter as KnowledgeSearchPortAdapter
    participant Search as SearchFaqUseCase
    participant Vector as PgVectorSearchAdapter
    participant PG as pgvector (PostgreSQL)
    participant AI as SpringAiChatAdapter
    participant GPT as OpenAI GPT-4o
    participant Publisher as EventPublisher

    UseCase->>Port: search(query, courseId)
    Port->>Adapter: search(query, courseId)
    Adapter->>Search: execute(query, courseId)
    Search->>Vector: search(query, topK=5, threshold=0.85)
    Vector->>PG: SELECT ... ORDER BY embedding <=> query_vector
    PG-->>Vector: 유사도 결과

    alt 유사도 >= 0.85 (FAQ HIT)
        Vector-->>Search: VectorDocument[]
        Search-->>Adapter: FaqSearchResult(faqId, question, answer, 0.91)
        Adapter-->>UseCase: Optional<SearchResult>

        UseCase->>Publisher: publishEvent(FaqSearchRequested.hit)
        Note over Publisher: → CoachingEventListener<br/>→ RecordEventUseCase<br/>→ FAQ_HIT 이벤트 저장

        UseCase->>AI: streamSocratic(message, FAQ 컨텍스트)
        Note over AI: 시스템 프롬프트에<br/>FAQ Q&A 추가
        AI->>GPT: ChatClient.stream()
        GPT-->>AI: SSE chunks
        AI-->>Student: FAQ 기반 코칭 응답 (스트리밍)

    else 유사도 < 0.85 (FAQ MISS)
        Vector-->>Search: 빈 결과
        Search-->>Adapter: Optional.empty()
        Adapter-->>UseCase: Optional.empty()

        UseCase->>Publisher: publishEvent(FaqSearchRequested.miss)
        Note over Publisher: → FAQ_MISS 이벤트 저장

        UseCase->>AI: streamSocratic(message, null)
        AI->>GPT: ChatClient.stream()
        GPT-->>Student: AI 직접 코칭 응답 (스트리밍)
    end
```

---

## 3. 용어 검색 플로우 (Sub Chat)

오른쪽 패널에서 학생이 용어/개념을 검색하면, TERM_LOOKUP 상태로 처리됩니다.

```mermaid
sequenceDiagram
    actor Student as 학생 (Frontend)
    participant Controller as ChatController
    participant UseCase as ProcessMessageUseCase
    participant Engine as CoachingEngine
    participant AI as SpringAiChatAdapter
    participant GPT as OpenAI GPT-4o
    participant Publisher as EventPublisher
    participant Listener as CoachingEventListener
    participant DB as PostgreSQL

    Student->>Controller: POST /api/v1/chat<br/>{chatType: "sub", message: "객체 참조가 뭐야?"}
    Controller->>UseCase: execute(Command)

    UseCase->>Engine: determine(message, SUB, turn)
    Note over Engine: chatType == SUB<br/>→ TERM_LOOKUP
    Engine-->>UseCase: TERM_LOOKUP

    UseCase->>Publisher: publishEvent(TermLookupRequested)
    Publisher-->>Listener: @Async
    Listener->>DB: INSERT learning_path_events<br/>(TERM_SEARCHED, "객체 참조가 뭐야?")

    UseCase->>AI: streamTermExplanation("객체 참조가 뭐야?")
    Note over AI: TERM_PROMPT:<br/>"용어를 쉽고 명확하게 설명"
    AI->>GPT: ChatClient.stream()
    GPT-->>AI: SSE chunks
    AI-->>Controller: Flux<String>
    Controller-->>Student: text/event-stream<br/>"객체 참조란 메모리 상의<br/>객체를 가리키는 주소값입니다..."
```

---

## 4. 피드백 & 세션 해결 플로우

학생이 "해결완료" 버튼을 누르면 세션이 resolved 상태로 전환되고 도메인 이벤트가 발행됩니다.

```mermaid
sequenceDiagram
    actor Student as 학생 (Frontend)
    participant Controller as ChatController
    participant UseCase as SubmitFeedbackUseCase
    participant Session as ChatSession
    participant Repo as ChatSessionRepository
    participant Publisher as EventPublisher
    participant Listener as CoachingEventListener
    participant Record as RecordEventUseCase
    participant DB as PostgreSQL

    Student->>Controller: POST /api/v1/chat/feedback<br/>{sessionId, resolved: true, summary: "해결됨"}
    Controller->>UseCase: execute(Command)

    UseCase->>Repo: findById(sessionId)
    Repo-->>UseCase: ChatSession

    UseCase->>Session: markResolved("해결됨")
    Note over Session: resolved = true<br/>resolvedAt = now()<br/>registerEvent(SessionResolved)

    UseCase->>Repo: save(session)
    UseCase->>Publisher: publishEvent(SessionResolved)

    Publisher-->>Listener: @Async @EventListener
    Listener->>Record: execute(RESOLVED, totalTurns, "해결됨")
    Record->>DB: INSERT learning_path_events

    Controller-->>Student: HTTP 200 OK
```

---

## 5. 학습 경로 타임라인 조회 (학생 복습)

학생이 이전 코칭 세션의 학습 경로를 타임라인으로 복습합니다.

```mermaid
sequenceDiagram
    actor Student as 학생 (Frontend)
    participant Controller as LearningPathController
    participant UseCase as GetTimelineUseCase
    participant Repo as LearningPathRepository
    participant DB as PostgreSQL

    Student->>Controller: GET /api/v1/learning-path/{sessionId}
    Controller->>UseCase: bySession(sessionId)
    UseCase->>Repo: findBySessionIdOrderByCreatedAtAsc(sessionId)
    Repo->>DB: SELECT * FROM learning_path_events<br/>WHERE session_id = ? ORDER BY created_at

    DB-->>Repo: LearningPathEvent[]
    Repo-->>UseCase: List<LearningPathEvent>

    Note over UseCase: Entity → TimelineResponse 변환
    UseCase-->>Controller: List<TimelineResponse>
    Controller-->>Student: JSON 응답

    Note over Student: 타임라인 시각화:<br/>STUDENT_QUESTION → 파란색<br/>SOCRATIC_QUESTION → 보라색<br/>TERM_SEARCHED → 주황색<br/>FAQ_HIT → 보라색 (유사도%)<br/>RESOLVED → 초록색 ✓
```

---

## 6. 강사 분석 API 플로우

강사가 과목별 학습 분석 데이터를 조회합니다.

```mermaid
sequenceDiagram
    actor Instructor as 강사 (Frontend)
    participant Controller as AnalyticsController
    participant UseCase as GetAnalyticsUseCase
    participant LPRepo as LearningPathRepository
    participant SRepo as ChatSessionRepository
    participant DB as PostgreSQL

    rect rgb(240, 248, 255)
        Note over Instructor,DB: 가장 많이 막힌 용어 Top N
        Instructor->>Controller: GET /api/v1/analytics/{courseId}/terms?limit=10
        Controller->>UseCase: topTerms(courseId, 10)
        UseCase->>LPRepo: findTopSearchedTerms(courseId, 10)
        LPRepo->>DB: SELECT content, COUNT(*)<br/>FROM learning_path_events<br/>WHERE event_type = 'TERM_SEARCHED'<br/>GROUP BY content ORDER BY count DESC
        DB-->>Instructor: [{term: "객체 참조", count: 45}, ...]
    end

    rect rgb(255, 248, 240)
        Note over Instructor,DB: 평균 해결 턴 수
        Instructor->>Controller: GET /api/v1/analytics/{courseId}/turns
        Controller->>UseCase: averageTurns(courseId)
        UseCase->>SRepo: findByCourseIdAndResolved(courseId)
        SRepo->>DB: SELECT * FROM chat_sessions<br/>WHERE course_id = ? AND resolved = true
        DB-->>Instructor: {averageTurns: 4.2}
    end

    rect rgb(240, 255, 240)
        Note over Instructor,DB: FAQ 히트율 추이 (월별)
        Instructor->>Controller: GET /api/v1/analytics/{courseId}/faq-hit-rate
        Controller->>UseCase: faqHitRate(courseId)
        UseCase->>LPRepo: findFaqHitRateByMonth(courseId)
        LPRepo->>DB: SELECT DATE_TRUNC('month', created_at),<br/>COUNT(*) FILTER (WHERE event_type='FAQ_HIT')<br/>* 100.0 / COUNT(*)
        DB-->>Instructor: [{month: "2026-03", hitRatePercent: 67.5}, ...]
    end
```

---

## 7. 전체 시스템 아키텍처 흐름

프론트엔드부터 DB까지 전체 요청 흐름을 요약합니다.

```mermaid
sequenceDiagram
    actor User as 학생/강사
    participant FE as Frontend<br/>(React + Vite)
    participant Proxy as Vite Proxy<br/>(:3000 → :21009)
    participant K3s as K3s NodePort<br/>(:21009)
    participant Boot as Spring Boot<br/>(Netty :8080)
    participant Security as SecurityConfig<br/>(OAuth2 / permitAll)
    participant Domain as Domain Layer<br/>(UseCase + Entity)
    participant AI as OpenAI GPT-4o
    participant PG as PostgreSQL<br/>+ pgvector
    participant KC as Keycloak<br/>(nadoceo realm)

    User->>FE: 메시지 입력
    FE->>Proxy: POST /api/v1/chat
    Proxy->>K3s: → localhost:21009
    K3s->>Boot: → Pod :8080

    opt JWT 인증 활성화 시
        Boot->>Security: JWT 검증
        Security->>KC: JWKS 공개키 조회
        KC-->>Security: realm_access.roles
        Security-->>Boot: ROLE_STUDENT 확인
    end

    Boot->>Domain: ProcessMessageUseCase
    Domain->>Domain: CoachingEngine.determine()

    alt FAQ_SEARCH
        Domain->>PG: pgvector 유사도 검색
        PG-->>Domain: FAQ 결과
    end

    Domain->>AI: ChatClient.stream()
    AI-->>Boot: SSE chunks (Flux<String>)

    par 비동기 이벤트 처리
        Domain->>PG: learning_path_events INSERT
    end

    Boot-->>K3s: text/event-stream
    K3s-->>Proxy: SSE
    Proxy-->>FE: SSE
    FE-->>User: 실시간 AI 응답 렌더링
```

---

## 8. Domain Event 흐름

coaching 컨텍스트에서 발행된 도메인 이벤트가 analytics 컨텍스트로 전달되는 과정입니다.

```mermaid
sequenceDiagram
    participant Session as ChatSession<br/>(AggregateRoot)
    participant UseCase as ProcessMessage<br/>UseCase
    participant Publisher as Application<br/>EventPublisher
    participant Listener as Coaching<br/>EventListener
    participant Record as RecordEvent<br/>UseCase
    participant DB as learning_path_events

    Note over Session: 도메인 이벤트 수집 (Transient)
    Session->>Session: registerEvent(StudentQuestionAsked)

    UseCase->>UseCase: sessionRepository.save(session)

    loop 수집된 이벤트마다
        UseCase->>Publisher: publishEvent(event)
        Session->>Session: clearDomainEvents()
    end

    Note over Publisher,Listener: Spring @Async @EventListener

    Publisher-->>Listener: StudentQuestionAsked
    Listener->>Record: execute(STUDENT_QUESTION, turn, content)
    Record->>DB: INSERT INTO learning_path_events

    Publisher-->>Listener: SocraticQuestionDelivered
    Listener->>Record: execute(SOCRATIC_QUESTION, turn, ...)
    Record->>DB: INSERT

    Publisher-->>Listener: FaqSearchRequested (hit/miss)
    Listener->>Record: execute(FAQ_HIT or FAQ_MISS, ...)
    Record->>DB: INSERT

    Publisher-->>Listener: TermLookupRequested
    Listener->>Record: execute(TERM_SEARCHED, term)
    Record->>DB: INSERT

    Publisher-->>Listener: SessionResolved
    Listener->>Record: execute(RESOLVED, totalTurns, summary)
    Record->>DB: INSERT
```
