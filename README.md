# NADOCEO Coaching AI — Backend

IT 교육 코칭 AI 백엔드. 소크라테스식 대화로 학생의 문제 해결을 유도하고, 벡터 DB FAQ + 학습 경로 추적으로 학생 복습 및 강사 분석을 지원합니다.

## Tech Stack

| 항목 | 기술 |
|---|---|
| Runtime | Java 23 (Virtual Threads, Record, Pattern Matching, Sealed Classes) |
| Framework | Spring Boot 3.3 + Spring WebFlux (SSE 스트리밍) |
| Architecture | DDD + Clean Architecture (Bounded Context, Ports & Adapters) |
| AI | Spring AI — OpenAI GPT-4o + text-embedding-3-small |
| DB | PostgreSQL + pgvector (벡터 유사도 검색) |
| Migration | Flyway |
| Auth | Keycloak OAuth2 (OIDC JWT Resource Server) |
| Docs | Swagger / OpenAPI 3 (springdoc-openapi) |
| Build | Gradle (Groovy DSL) |
| Deploy | K3s (로컬 Kubernetes, NodePort 21009) |

## Getting Started

```bash
# 빌드
./gradlew build

# 개발 서버 실행 (포트 8080)
./gradlew bootRun

# Docker 이미지 빌드
docker build -t nadoceo-backend .
```

### 환경 변수

| 변수 | 설명 | 기본값 |
|---|---|---|
| `DB_URL` | PostgreSQL 접속 URL | `jdbc:postgresql://localhost:5432/nadoceo` |
| `DB_USERNAME` | DB 사용자 | `nadoceo` |
| `DB_PASSWORD` | DB 비밀번호 | `nadoceo` |
| `OPENAI_API_KEY` | OpenAI API 키 | — |
| `KEYCLOAK_ISSUER_URI` | Keycloak Realm URL | `http://localhost:30808/realms/nadoceo` |

## Architecture — DDD + Clean Architecture

### Bounded Contexts

```
coaching     소크라테스 코칭 플로우, AI SSE 스트리밍 대화
knowledge    FAQ 관리, 벡터 유사도 검색, 임베딩
analytics    학습 경로 이벤트 기록, 강사 분석 쿼리
identity     사용자, 학원, 과목
shared       Value Objects, DomainEvent, SecurityConfig
```

### Layer 구조

```
{context}/
├── domain/          엔티티, Value Object, Repository Port, Domain Service
├── application/     Use Case, Output Port
├── infrastructure/  JPA 구현, AI 어댑터, 이벤트 리스너
└── presentation/    REST Controller, DTO
```

### Context 간 통신

- **coaching → analytics**: Domain Event (Spring `ApplicationEventPublisher` + `@Async`)
- **coaching → knowledge**: Output Port (`KnowledgeSearchPort` 인터페이스)

```
[ChatController] → [ProcessMessageUseCase] → [AiChatPort] → [SpringAiChatAdapter] → OpenAI
                                            → [KnowledgeSearchPort] → [SearchFaqUseCase] → pgvector
                    domain events ─────────→ [CoachingEventListener] → [RecordEventUseCase] → DB
```

## API Endpoints

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/v1/chat` | 메시지 전송 + SSE 스트리밍 | student |
| GET | `/api/v1/chat/{sessionId}` | 세션 히스토리 | student |
| POST | `/api/v1/chat/feedback` | 피드백 (해결/미해결) | student |
| GET | `/api/v1/faq/{courseId}` | 과목 FAQ 목록 | student |
| POST | `/api/v1/faq` | FAQ 등록 | instructor |
| POST | `/api/v1/faq/{faqId}/upvote` | FAQ 좋아요 | student |
| GET | `/api/v1/learning-path/{sessionId}` | 세션 타임라인 | student |
| GET | `/api/v1/learning-path/my?studentId=` | 내 학습 경로 | student |
| GET | `/api/v1/analytics/{courseId}/terms` | 검색 용어 Top N | instructor |
| GET | `/api/v1/analytics/{courseId}/turns` | 평균 해결 턴 수 | instructor |
| GET | `/api/v1/analytics/{courseId}/faq-hit-rate` | FAQ 히트율 추이 | instructor |
| GET | `/api/v1/analytics/{courseId}/students` | 학생별 패턴 | instructor |

**Swagger UI**: http://localhost:21009/swagger-ui.html

## Keycloak

- **Realm**: `nadoceo` (import: `keycloak/nadoceo-realm.json`)
- **Roles**: `student`, `instructor`, `admin` (계층형)
- **Clients**: `nadoceo-backend` (Confidential), `nadoceo-frontend` (Public SPA + PKCE)

## K3s Deployment

```bash
# 1. Docker 이미지 빌드 & 로컬 레지스트리 push
docker build -t localhost:5000/nadoceo-backend:latest .
docker push localhost:5000/nadoceo-backend:latest

# 2. Keycloak Realm 등록 (Admin Console → Import)
# 파일: keycloak/nadoceo-realm.json

# 3. K3s 배포
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/secret.yml      # OPENAI_API_KEY 설정 필요
kubectl apply -f k8s/configmap.yml
kubectl apply -f k8s/postgres/
kubectl apply -f k8s/deployment.yml
kubectl apply -f k8s/service.yml      # NodePort 21009
```

## Related

- **Frontend**: [nadoCEO_FE](https://github.com/kimuragen23/nadoCEO_FE)
