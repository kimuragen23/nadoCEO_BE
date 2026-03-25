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

export function streamChat(
  body: ChatRequestBody,
  onChunk: (chunk: string) => void,
  onDone: () => void,
  onError: (err: Error) => void,
): AbortController {
  const controller = new AbortController();

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
          if (line.startsWith('data:')) {
            onChunk(line.slice(5));
          } else if (line.trim().length > 0) {
            onChunk(line);
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

// --- Session History API ---

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

export function getFaqs(courseId: string) {
  return apiFetch<any[]>(`/faq/${courseId}`);
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
