const API_BASE = '/api/v1';

export async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
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
    headers: { 'Content-Type': 'application/json' },
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
        // SSE 포맷: "data:chunk\n\n" 파싱
        const lines = text.split('\n');
        for (const line of lines) {
          if (line.startsWith('data:')) {
            onChunk(line.slice(5));
          } else if (line.trim().length > 0) {
            // plain text 스트리밍
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

// --- FAQ API ---

export function getFaqs(courseId: string) {
  return apiFetch<any[]>(`/faq/${courseId}`);
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
