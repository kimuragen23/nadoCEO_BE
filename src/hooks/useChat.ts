import { useState, useCallback, useRef } from 'react';
import { useChatStore } from '../store/chatStore';
import { Message } from '../types/chat';
import { streamChat } from '../api/client';

// 임시 하드코딩 (추후 Keycloak 인증 후 동적으로)
const STUDENT_ID = '00000000-0000-0000-0000-000000000001';
const COURSE_ID = '00000000-0000-0000-0000-000000000001';

export function useChat(type: 'main' | 'sub') {
  const store = useChatStore();
  const [input, setInput] = useState('');
  const abortRef = useRef<AbortController | null>(null);

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

      // 빈 assistant 메시지 생성 (스트리밍 채울 용도)
      const assistantMsg: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: '',
        createdAt: new Date().toISOString(),
      };
      store.appendMainMessage(assistantMsg);

      // 백엔드 SSE 스트리밍
      abortRef.current = streamChat(
        {
          sessionId: store.mainSessionId,
          message,
          courseId: COURSE_ID,
          studentId: STUDENT_ID,
          chatType: 'main',
        },
        (chunk) => {
          store.appendStreamChunk(chunk);
        },
        () => {
          store.setStreaming(false);
        },
        (err) => {
          console.error('Chat stream error:', err);
          store.appendStreamChunk('\n\n[오류가 발생했습니다. 다시 시도해주세요.]');
          store.setStreaming(false);
        },
      );
    } else {
      // Sub chat (용어 검색)
      store.appendSubMessage(userMsg);
      store.addSearchedTerm(message);

      const assistantMsg: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: '',
        createdAt: new Date().toISOString(),
      };
      store.appendSubMessage(assistantMsg);

      // Sub chat도 백엔드 SSE 스트리밍
      store.setSubStreaming(true);
      streamChat(
        {
          sessionId: store.subSessionId,
          message,
          courseId: COURSE_ID,
          studentId: STUDENT_ID,
          chatType: 'sub',
        },
        (chunk) => {
          store.appendSubStreamChunk(chunk);
        },
        () => {
          store.setSubStreaming(false);
        },
        (err) => {
          console.error('Sub chat error:', err);
          store.appendSubStreamChunk('\n\n[오류가 발생했습니다.]');
          store.setSubStreaming(false);
        },
      );
    }

    setInput('');
  }, [type, store]);

  return {
    input,
    setInput,
    sendMessage,
  };
}
