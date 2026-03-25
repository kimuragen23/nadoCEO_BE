# NADOCEO Coaching AI — Backend

## Project Overview

IT 교육 코칭 AI 백엔드. 소크라테스식 대화로 학생의 문제 해결을 유도하고,
벡터 DB FAQ + 학습 경로 추적(Learning Path)으로 학생 복습 및 강사 분석을 지원한다.

## Tech Stack

- **Runtime**: Java 23 (Virtual Threads, Record, Pattern Matching, Sealed Classes)
- **Framework**: Spring Boot 3.3 + Spring WebFlux (SSE 스트리밍)
- **Architecture**: DDD + Clean Architecture (Bounded Context, Ports & Adapters)
- **AI**: Spring AI — 멀티 프로바이더 (Claude / GPT / Gemini)
- **DB**: PostgreSQL + pgvector (벡터 유사도 검색)
- **Migration**: Flyway
- **Auth**: Keycloak OAuth2 (OIDC JWT Resource Server)
- **Docs**: Swagger / OpenAPI 3 (springdoc-openapi)
- **Build**: Gradle (Groovy DSL)
- **Deploy**: K3s (로컬 Kubernetes, NodePort 21009)

## Commands

```bash
./gradlew bootRun         # 개발 서버 실행 (포트 8080)
./gradlew build           # 빌드
./gradlew test            # 테스트
./gradlew bootJar         # JAR 생성
docker build -t nadoceo-backend .  # Docker 이미지 빌드
```

## Architecture — DDD + Clean Architecture

### Bounded Contexts

| Context | 역할 | 핵심 Aggregate |
|---|---|---|
| **coaching** | 소크라테스 코칭 플로우, AI 대화 | ChatSession |
| **knowledge** | FAQ 관리, 벡터 검색, 임베딩 | Faq |
| **analytics** | 학습 경로 이벤트 기록, 강사 분석 | LearningPathEvent |
| **identity** | 사용자, 학원, 과목 (CRUD) | User, Academy, Course |
| **shared** | 공통 Value Object, DomainEvent 기반 | — |

### Layer 구조 (각 Bounded Context)

```
{context}/
├── domain/          # 엔티티, Value Object, Repository Port (interface), Domain Service
├── application/     # Use Case, Output Port (interface)
├── infrastructure/  # JPA 구현, AI 어댑터, 이벤트 리스너
└── presentation/    # REST Controller, Request/Response DTO
```

### 의존성 흐름

```
Presentation → Application → Domain
                    ↓
              Infrastructure (implements Domain ports)
```

### Context 간 통신

- **coaching → analytics**: Domain Event (`ApplicationEventPublisher`)
  - `StudentQuestionAsked`, `SocraticQuestionDelivered`, `FaqSearchRequested`, `TermLookupRequested`, `SessionResolved`
  - `CoachingEventListener`가 구독 → `RecordEventUseCase` 호출
- **coaching → knowledge**: Output Port (`KnowledgeSearchPort`)
  - coaching이 인터페이스 정의, knowledge가 `KnowledgeSearchPortAdapter`로 구현

## Project Structure

```
src/main/java/com/nadoceo/
├── NadoceoApplication.java
│
├── shared/
│   ├── domain/
│   │   ├── DomainEvent.java              # 도메인 이벤트 인터페이스
│   │   ├── AggregateRoot.java            # 이벤트 수집 베이스 클래스
│   │   ├── SessionId.java                # Value Object
│   │   ├── StudentId.java
│   │   ├── CourseId.java
│   │   └── FaqId.java
│   └── config/
│       ├── SecurityConfig.java
│       └── DomainEventConfig.java        # @EnableAsync
│
├── coaching/                              # 코칭 Bounded Context
│   ├── domain/
│   │   ├── ChatSession.java              # Aggregate Root (advanceTurn, markResolved)
│   │   ├── ChatType.java                 # enum: MAIN, SUB
│   │   ├── CoachingState.java            # enum: SOCRATIC, FAQ_SEARCH, CONTINUE, TERM_LOOKUP
│   │   ├── CoachingEngine.java           # Domain Service (순수 로직, Spring 의존 없음)
│   │   ├── MessageAnalyzer.java          # 메시지 패턴 분석
│   │   ├── ChatSessionRepository.java    # Port (interface)
│   │   └── event/                        # Domain Events
│   │       ├── StudentQuestionAsked.java
│   │       ├── SocraticQuestionDelivered.java
│   │       ├── FaqSearchRequested.java
│   │       ├── TermLookupRequested.java
│   │       └── SessionResolved.java
│   ├── application/
│   │   ├── ProcessMessageUseCase.java    # 코칭 플로우 오케스트레이션 → Flux<String> SSE
│   │   ├── SubmitFeedbackUseCase.java
│   │   ├── GetSessionUseCase.java
│   │   └── port/
│   │       ├── AiChatPort.java           # Output Port (AI 추상화) → Flux<String>
│   │       └── KnowledgeSearchPort.java  # Output Port (FAQ 검색)
│   ├── infrastructure/
│   │   ├── persistence/
│   │   │   ├── JpaChatSessionRepository.java
│   │   │   └── SpringDataChatSessionRepo.java
│   │   └── ai/
│   │       ├── SpringAiChatAdapter.java  # AiChatPort 구현 (SSE 스트리밍)
│   │       └── AiProviderConfig.java     # 멀티 프로바이더 빈 선택
│   └── presentation/
│       ├── ChatController.java
│       └── dto/
│
├── knowledge/                             # FAQ Bounded Context
│   ├── domain/
│   │   ├── Faq.java                      # Aggregate Root (upvote, isPublicReady)
│   │   ├── FaqSource.java                # enum: STUDENT, INSTRUCTOR
│   │   ├── FaqRepository.java            # Port
│   │   ├── VectorSearchPort.java         # Port (벡터 검색 추상화)
│   │   └── FaqSearchResult.java          # Value Object
│   ├── application/
│   │   ├── SearchFaqUseCase.java
│   │   ├── CreateFaqUseCase.java
│   │   ├── UpvoteFaqUseCase.java
│   │   └── ListFaqsUseCase.java
│   ├── infrastructure/
│   │   ├── persistence/
│   │   ├── embedding/
│   │   │   └── PgVectorSearchAdapter.java
│   │   └── KnowledgeSearchPortAdapter.java  # coaching Port 구현
│   └── presentation/
│       ├── FaqController.java
│       └── dto/
│
├── analytics/                             # 분석 Bounded Context
│   ├── domain/
│   │   ├── LearningPathEvent.java
│   │   ├── EventType.java
│   │   └── LearningPathRepository.java   # Port
│   ├── application/
│   │   ├── RecordEventUseCase.java
│   │   ├── GetTimelineUseCase.java
│   │   └── GetAnalyticsUseCase.java
│   ├── infrastructure/
│   │   ├── persistence/
│   │   └── event/
│   │       └── CoachingEventListener.java  # Domain Event 구독
│   └── presentation/
│       ├── LearningPathController.java
│       └── AnalyticsController.java
│
└── identity/
    ├── domain/
    │   ├── Academy.java
    │   ├── Course.java
    │   ├── User.java
    │   └── UserRole.java
    └── infrastructure/persistence/
```

## AI Provider Configuration

`application.yml`의 `nadoceo.ai.provider` 값으로 AI 프로바이더 전환:

| 값 | 프로바이더 | 모델 |
|---|---|---|
| `anthropic` | Claude | claude-sonnet-4-20250514 |
| `openai` | GPT | gpt-4o |
| `vertexai` | Gemini | gemini-2.0-flash |

환경변수 `AI_PROVIDER`로도 설정 가능.

## API Endpoints

| Method | Path | 설명 |
|---|---|---|
| POST | `/api/v1/chat` | 메시지 전송 + SSE 스트리밍 |
| GET | `/api/v1/chat/{sessionId}` | 세션 히스토리 |
| POST | `/api/v1/chat/feedback` | 피드백 (해결/미해결) |
| GET | `/api/v1/faq/{courseId}` | 과목 FAQ 목록 |
| POST | `/api/v1/faq` | FAQ 등록 |
| POST | `/api/v1/faq/{faqId}/upvote` | FAQ 좋아요 |
| GET | `/api/v1/learning-path/{sessionId}` | 세션 타임라인 |
| GET | `/api/v1/learning-path/my?studentId=` | 내 학습 경로 |
| GET | `/api/v1/analytics/{courseId}/terms` | 검색 용어 Top N |
| GET | `/api/v1/analytics/{courseId}/turns` | 평균 해결 턴 수 |
| GET | `/api/v1/analytics/{courseId}/faq-hit-rate` | FAQ 히트율 추이 |
| GET | `/api/v1/analytics/{courseId}/students` | 학생별 패턴 |

## Keycloak Integration

### Realm 설정
- **Realm**: `nadoceo`
- **Realm JSON**: `keycloak/nadoceo-realm.json` (Import용)
- **Keycloak URL**: `https://keycloak.gajok.app` (외부) / `keycloak.default.svc.cluster.local:8080` (K3s 내부)

### Roles
| Role | 설명 | 접근 가능 API |
|---|---|---|
| `student` | 학생 | 코칭 채팅, FAQ 조회, 학습 경로 |
| `instructor` | 강사 (student 포함) | + FAQ 등록, Analytics |
| `admin` | 관리자 (instructor 포함) | 전체 |

### Clients
| Client ID | Type | 용도 |
|---|---|---|
| `nadoceo-backend` | Confidential | 백엔드 Service Account |
| `nadoceo-frontend` | Public (PKCE) | 프론트엔드 SPA |

### JWT → Spring Security 매핑
- Keycloak `realm_access.roles` → `ROLE_STUDENT`, `ROLE_INSTRUCTOR`, `ROLE_ADMIN`
- `SecurityConfig.KeycloakRealmRoleConverter` 에서 변환

### 기본 테스트 계정 (임시 비밀번호)
- `admin` / `admin` (admin 역할)
- `instructor01` / `instructor01` (instructor 역할)
- `student01` / `student01` (student 역할)

## Swagger / OpenAPI

- **Swagger UI**: `http://localhost:21009/swagger-ui.html`
- **API Docs (JSON)**: `http://localhost:21009/v3/api-docs`
- Keycloak OAuth2 인증 연동 (Swagger UI에서 직접 토큰 발급 가능)

## K3s Deployment

```bash
# 1. Docker 이미지 빌드 & K3s로 가져오기
docker build -t nadoceo-backend .
docker save nadoceo-backend:latest | sudo k3s ctr images import -

# 2. Keycloak Realm 등록
# Keycloak Admin Console → Create Realm → Import JSON
# 파일: keycloak/nadoceo-realm.json

# 3. K3s 리소스 배포
kubectl apply -f k8s/namespace.yml
vi k8s/secret.yml  # API 키, DB 비밀번호 수정
kubectl apply -f k8s/secret.yml
kubectl apply -f k8s/configmap.yml
kubectl apply -f k8s/postgres/
kubectl apply -f k8s/deployment.yml
kubectl apply -f k8s/service.yml  # NodePort 21009

# 4. 확인
kubectl get all -n nadoceo
curl http://localhost:21009/actuator/health
```

### 접속 URL
| 용도 | URL |
|---|---|
| API | `http://localhost:21009/api/v1/...` |
| Swagger UI | `http://localhost:21009/swagger-ui.html` |
| Health Check | `http://localhost:21009/actuator/health` |
| Keycloak Admin | `https://keycloak.gajok.app/admin` |

## Key Design Decisions

- **DDD Bounded Context**: coaching/knowledge/analytics/identity로 도메인 분리
- **Domain Events**: coaching에서 analytics로의 이벤트 전달은 Spring ApplicationEventPublisher + @Async
- **Ports & Adapters**: AiChatPort, KnowledgeSearchPort, VectorSearchPort로 외부 의존성 역전
- **Rich Domain Model**: ChatSession.advanceTurn(), Faq.upvote() 등 비즈니스 로직을 엔티티에 응집
- **CoachingEngine**: Spring 의존 없는 순수 Domain Service (Pattern Matching)
- **SSE 스트리밍**: Flux<String> 체인이 Controller → UseCase → Port → Adapter까지 유지
- **멀티 AI 프로바이더**: AiProviderConfig에서 설정값 기반 ChatModel 빈 선택
- **Keycloak OAuth2**: JWT Resource Server로 역할 기반 접근 제어 (RBAC)
- **Swagger/OpenAPI**: springdoc-openapi로 API 문서 자동 생성, Keycloak OAuth2 인증 연동
