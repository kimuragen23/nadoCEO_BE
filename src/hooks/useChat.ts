import { useState, useCallback, useRef } from 'react';
import { useChatStore } from '../store/chatStore';
import { useAuthStore } from '../store/authStore';
import { Message } from '../types/chat';
import { streamChat } from '../api/client';

const DEFAULT_COURSE_ID = '00000000-0000-0000-0000-000000000001';

export function useChat(type: 'main' | 'sub') {
  const store = useChatStore();
  const { userId } = useAuthStore();
  const [input, setInput] = useState('');
  const abortRef = useRef<AbortController | null>(null);

  const studentId = userId || '00000000-0000-0000-0000-000000000001';

  const sendMessage = useCallback(async (message: string) => {
    if (!message.trim()) return;

    const userMsg: Message = {
      id: Date.now().toString(),
      role: 'user',
      content: message,
      createdAt: new Date().toISOString(),
    };

    if (type === 'main') {
      store.appendMainMessage(userMsg);
      store.setStreaming(true);
      store.incrementTurn();

      const assistantMsg: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: '',
        createdAt: new Date().toISOString(),
      };
      store.appendMainMessage(assistantMsg);

      abortRef.current = streamChat(
        {
          sessionId: store.mainSessionId,
          message,
          courseId: DEFAULT_COURSE_ID,
          studentId,
          chatType: 'main',
        },
        (chunk) => store.appendStreamChunk(chunk),
        () => store.setStreaming(false),
        (err) => {
          console.error('Chat stream error:', err);
          store.appendStreamChunk('\n\n[오류가 발생했습니다. 다시 시도해주세요.]');
          store.setStreaming(false);
        },
        (sessionId) => {
          store.setMainSessionId(sessionId);
        },
        (faq) => {
          store.addFaqHitToLastMessage({
            faqId: faq.faqId,
            similarity: faq.similarity,
            question: faq.question,
            answer: faq.answer,
          });
        },
      );
    } else {
      store.appendSubMessage(userMsg);
      store.addSearchedTerm(message);

      const assistantMsg: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: '',
        createdAt: new Date().toISOString(),
      };
      store.appendSubMessage(assistantMsg);

      store.setSubStreaming(true);
      streamChat(
        {
          sessionId: store.subSessionId,
          message,
          courseId: DEFAULT_COURSE_ID,
          studentId,
          chatType: 'sub',
        },
        (chunk) => store.appendSubStreamChunk(chunk),
        () => store.setSubStreaming(false),
        (err) => {
          console.error('Sub chat error:', err);
          store.appendSubStreamChunk('\n\n[오류가 발생했습니다.]');
          store.setSubStreaming(false);
        },
        (sessionId) => {
          store.setSubSessionId(sessionId);
        },
      );
    }

    setInput('');
  }, [type, store, studentId]);

  return {
    input,
    setInput,
    sendMessage,
  };
}
