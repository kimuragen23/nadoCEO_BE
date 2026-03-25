import { useAuthStore } from '../store/authStore';

const API_BASE = '/api/v1';

function getAuthHeaders(): Record<string, string> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  const token = useAuthStore.getState().token;
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

export async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      ...getAuthHeaders(),
      ...options?.headers,
    },
    ...options,
  });

  if (!res.ok) {
    throw new Error(`API error: ${res.status} ${res.statusText}`);
  }

  return res.json();
}

// --- Chat API ---

export interface ChatRequestBody {
  sessionId?: string | null;
  message: string;
  courseId: string;
  studentId: string;
  chatType?: string;
}

export interface FaqHitInfo {
  faqId: string;
  similarity: number;
  question: string;
  answer?: string;
}

export function streamChat(
  body: ChatRequestBody,
  onChunk: (chunk: string) => void,
  onDone: () => void,
  onError: (err: Error) => void,
  onSessionId?: (sessionId: string) => void,
  onFaqHit?: (faq: FaqHitInfo) => void,
): AbortController {
  const controller = new AbortController();
  // SSE data: prefix 후 %% → %로 변환될 수 있으므로 둘 다 처리
  const isSessionId = (s: string) => s.includes('SESSION_ID%');
  const isFaqHit = (s: string) => s.includes('FAQ_HIT%');

  fetch(`${API_BASE}/chat`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(body),
    signal: controller.signal,
  })
    .then(async (res) => {
      if (!res.ok) throw new Error(`Chat API error: ${res.status}`);
      const reader = res.body?.getReader();
      if (!reader) throw new Error('No response body');

      const decoder = new TextDecoder();
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        const text = decoder.decode(value, { stream: true });
        const lines = text.split('\n');
        for (const line of lines) {
          const content = line.startsWith('data:') ? line.slice(5) : line.trim();
          if (!content) continue;
          if (isSessionId(content)) {
            const id = content.replace(/^%+SESSION_ID%+/, '');
            onSessionId?.(id);
          } else if (isFaqHit(content)) {
            try {
              const jsonStr = content.replace(/^%+FAQ_HIT%+/, '');
              const faq = JSON.parse(jsonStr);
              onFaqHit?.(faq);
            } catch {}
          } else {
            onChunk(content);
          }
        }
      }
      onDone();
    })
    .catch((err) => {
      if (err.name !== 'AbortError') onError(err);
    });

  return controller;
}

// --- Feedback API ---

export interface FeedbackBody {
  sessionId: string;
  resolved: boolean;
  summary?: string;
}

export function submitFeedback(body: FeedbackBody) {
  return apiFetch<void>('/chat/feedback', {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

// --- Session API ---

export interface SessionDetail {
  id: string;
  studentId: string;
  courseId: string;
  chatType: string;
  messages: string; // JSON string
  totalTurns: number;
  resolved: boolean;
  createdAt: string;
}

export function getSession(sessionId: string) {
  return apiFetch<SessionDetail>(`/chat/${sessionId}`);
}

export interface SessionSummary {
  id: string;
  title: string;
  totalTurns: number;
  resolved: boolean;
  createdAt: string;
}

export function getSessionHistory(studentId: string) {
  return apiFetch<SessionSummary[]>(`/chat/history?studentId=${studentId}`);
}

// --- FAQ API ---

export interface FaqItem {
  id: string;
  courseId: string;
  question: string;
  answer: string;
  upvotes: number;
  source: string;
  createdAt: string;
}

export function getFaqs(courseId: string) {
  return apiFetch<FaqItem[]>(`/faq/${courseId}`);
}

// --- Glossary API ---

export interface GlossaryTerm {
  term: string;
  lastSearchedAt: string;
}

export function getGlossary(studentId: string) {
  return apiFetch<GlossaryTerm[]>(`/learning-path/glossary?studentId=${studentId}`);
}

// --- Course API ---

export interface CourseInfo {
  id: string;
  name: string;
  description: string;
}

export function getCourses() {
  return apiFetch<CourseInfo[]>('/courses');
}

// --- User API ---

export interface UserProfile {
  id: string;
  name: string;
  role: string;
}

export function getUserProfile(userId: string) {
  return apiFetch<UserProfile>(`/user/${userId}`);
}

// --- Learning Path API ---

export interface TimelineEvent {
  id: string;
  sessionId: string;
  eventType: string;
  turnNumber: number;
  content: string;
  faqHitId?: string;
  metadata?: string;
  createdAt: string;
}

export function getSessionTimeline(sessionId: string) {
  return apiFetch<TimelineEvent[]>(`/learning-path/${sessionId}`);
}

export function getMyLearningPath(studentId: string) {
  return apiFetch<TimelineEvent[]>(`/learning-path/my?studentId=${studentId}`);
}
