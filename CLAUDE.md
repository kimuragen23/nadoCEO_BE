# NADOCEO Coaching AI — Frontend

## Rules

- 코드 수정 후 반드시 git commit + push 할 것 (브랜치 전략: feature → develop → main)
- 프론트 수정 시 `tsc --noEmit` 타입 체크 통과 확인할 것
- Vite HMR로 자동 반영되지만, proxy 설정 변경 시 dev 서버 재시작 필요

## Project Overview

IT 교육 현장에서 학생의 질문에 소크라테스식 대화로 코칭하는 AI 서비스의 프론트엔드.
벡터 DB FAQ 기반 지식 베이스 + 학습 경로 추적(Learning Path)을 통해 학생 복습과 강사 분석을 지원한다.

## Tech Stack

- **Framework**: React 19 + TypeScript 5.8
- **Build**: Vite 6
- **Styling**: Tailwind CSS 4 + shadcn/ui
- **State**: Zustand 5
- **Routing**: React Router 7
- **Animation**: Motion (framer-motion)
- **AI**: Google GenAI SDK (프론트 측 연동)
- **Icons**: Lucide React

## Project Structure

```
src/
├── App.tsx                          # 라우팅 설정
├── main.tsx                         # 엔트리포인트
├── index.css                        # 글로벌 스타일
├── components/
│   ├── chat/                        # 채팅 관련 컴포넌트
│   │   ├── DualChatLayout.tsx       # 듀얼 채팅 레이아웃 (메인 + 서브)
│   │   ├── MainChatPanel.tsx        # 왼쪽 코칭 채팅
│   │   ├── SubChatPanel.tsx         # 오른쪽 용어 검색 채팅
│   │   ├── ChatInput.tsx            # 메시지 입력
│   │   ├── ChatMessageList.tsx      # 메시지 목록
│   │   ├── AiMessage.tsx            # AI 메시지 버블
│   │   ├── UserMessage.tsx          # 사용자 메시지 버블
│   │   ├── FaqHitBadge.tsx          # FAQ 히트 배지
│   │   ├── FaqHitCards.tsx          # FAQ 히트 카드 목록
│   │   ├── FeedbackButtons.tsx      # 해결/미해결 피드백 버튼
│   │   ├── LearningPathBar.tsx      # 하단 학습경로 바
│   │   └── Sidebar.tsx              # 사이드바
│   ├── learning-path/               # 학습 경로 컴포넌트
│   │   ├── PathHeader.tsx           # 경로 헤더
│   │   └── TurnTimeline.tsx         # 턴별 타임라인
│   └── ui/                          # shadcn/ui 기본 컴포넌트
├── pages/
│   ├── chat/ChatPage.tsx            # 채팅 페이지
│   └── learning-path/LearningPathPage.tsx  # 학습 경로 페이지
├── hooks/
│   └── useChat.ts                   # 채팅 커스텀 훅
├── store/
│   └── chatStore.ts                 # Zustand 채팅 상태 관리
├── types/
│   ├── chat.ts                      # 채팅 타입 정의
│   └── learningPath.ts              # 학습 경로 타입 정의
└── lib/
    └── utils.ts                     # 유틸리티 (cn 등)
```

## Commands

```bash
npm run dev      # 개발 서버 (포트 3000)
npm run build    # 프로덕션 빌드
npm run lint     # TypeScript 타입 체크 (tsc --noEmit)
npm run preview  # 빌드 프리뷰
npm run clean    # dist 폴더 삭제
```

## Architecture & Design Decisions

### 듀얼 채팅 UI
- **왼쪽 (Main)**: 소크라테스식 코칭 대화 — 문제 해결 유도
- **오른쪽 (Sub)**: 용어/개념 검색용 보조 채팅
- **하단 바**: 학습 경로 타임라인 (이벤트 시각화)

### 핵심 플로우
1. 학생이 질문 → AI가 소크라테스식 역질문 (최대 2회)
2. 벡터 DB에서 FAQ 유사도 검색 (threshold ≥ 0.85)
3. FAQ 히트 시 → FAQ 기반 답변, 미스 시 → AI API 호출
4. 해결 피드백 시 → FAQ로 저장 + 학습 경로 기록

### 학습 경로 이벤트 타입
- `STUDENT_QUESTION` — 학생 질문
- `SOCRATIC_QUESTION` — AI 역질문
- `TERM_SEARCHED` — 용어 검색
- `FAQ_HIT` / `FAQ_MISS` — FAQ 매칭 결과
- `WEB_SEARCHED` — 웹 검색
- `RESOLVED` — 문제 해결

## Backend API Endpoints (연동 대상)

| Method | Path | 설명 |
|---|---|---|
| POST | `/api/v1/chat` | 메시지 전송 + SSE 스트리밍 응답 |
| GET | `/api/v1/chat/{sessionId}` | 세션 히스토리 조회 |
| POST | `/api/v1/faq/feedback` | 피드백 제출 + FAQ 저장 |
| GET | `/api/v1/faq/{courseId}` | 과목 FAQ 목록 |
| GET | `/api/v1/learning-path/{sessionId}` | 세션 타임라인 조회 |
| GET | `/api/v1/learning-path/my` | 내 학습 경로 목록 |

## Conventions

- UI 컴포넌트는 `components/ui/`에 shadcn/ui 기반으로 배치
- 도메인 컴포넌트는 `components/{domain}/` 디렉토리에 배치
- 상태 관리는 Zustand store 사용 (`store/` 디렉토리)
- 타입 정의는 `types/` 디렉토리에 분리
- CSS는 Tailwind 유틸리티 클래스 사용, `cn()` 헬퍼로 조건부 클래스 병합
