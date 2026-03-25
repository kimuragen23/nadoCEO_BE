# NADOCEO Coaching AI — Frontend

IT 교육 코칭 AI 프론트엔드. 소크라테스식 대화 UI + FAQ 벡터 검색 + 학습 경로 시각화.

## Tech Stack

- **React 19** + TypeScript 5.8
- **Vite 6** (빌드 + 개발 서버)
- **Tailwind CSS 4** + shadcn/ui
- **Zustand 5** (상태 관리)
- **React Router 7** (라우팅)

## Getting Started

```bash
# 의존성 설치
npm install

# 개발 서버 실행 (포트 3000)
npm run dev

# 빌드
npm run build

# 타입 체크
npm run lint
```

## Architecture

### 듀얼 채팅 UI

| 패널 | 역할 | 색상 |
|---|---|---|
| 왼쪽 (Main) | 소크라테스식 코칭 대화 | Blue |
| 오른쪽 (Sub) | 용어/개념 검색 | Amber |
| 하단 바 | 학습 경로 타임라인 | — |

### 프로젝트 구조

```
src/
├── api/
│   └── client.ts              # 백엔드 API 클라이언트 (SSE 스트리밍)
├── components/
│   ├── chat/                   # 채팅 컴포넌트
│   │   ├── DualChatLayout.tsx  # 듀얼 레이아웃 (메인 + 서브)
│   │   ├── MainChatPanel.tsx   # 코칭 채팅 패널
│   │   ├── SubChatPanel.tsx    # 용어 검색 패널
│   │   ├── ChatInput.tsx       # 메시지 입력
│   │   ├── ChatMessageList.tsx # 메시지 목록
│   │   ├── AiMessage.tsx       # AI 응답 버블
│   │   ├── UserMessage.tsx     # 사용자 메시지 버블
│   │   ├── FaqHitCards.tsx     # FAQ 추천 카드
│   │   ├── FeedbackButtons.tsx # 해결/미해결 피드백
│   │   ├── LearningPathBar.tsx # 하단 학습경로 바
│   │   └── Sidebar.tsx         # 사이드바
│   ├── learning-path/          # 학습 경로 컴포넌트
│   │   ├── PathHeader.tsx
│   │   └── TurnTimeline.tsx
│   └── ui/                     # shadcn/ui 컴포넌트
├── hooks/
│   └── useChat.ts              # 채팅 훅 (SSE 스트리밍 연동)
├── store/
│   └── chatStore.ts            # Zustand 상태 관리
├── pages/
│   ├── chat/ChatPage.tsx
│   └── learning-path/LearningPathPage.tsx
├── types/
│   ├── chat.ts                 # Message, FaqHit 타입
│   └── learningPath.ts         # LearningPathEvent 타입
└── lib/
    └── utils.ts                # cn() 유틸리티
```

## Backend Integration

백엔드 API (`localhost:21009`)와 Vite proxy로 연동:

```
프론트 (localhost:3000) → Vite proxy → 백엔드 (localhost:21009)
```

### API Endpoints

| Method | Path | 설명 |
|---|---|---|
| POST | `/api/v1/chat` | 메시지 전송 + SSE 스트리밍 |
| GET | `/api/v1/chat/{sessionId}` | 세션 히스토리 |
| POST | `/api/v1/chat/feedback` | 피드백 제출 |
| GET | `/api/v1/faq/{courseId}` | FAQ 목록 |
| GET | `/api/v1/learning-path/{sessionId}` | 학습 경로 타임라인 |

## Related

- **Backend**: [nadoCEO_BE](https://github.com/kimuragen23/nadoCEO_BE)
- **Swagger UI**: http://localhost:21009/swagger-ui.html
