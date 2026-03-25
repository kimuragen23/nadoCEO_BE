import React, { useEffect, useRef, useLayoutEffect } from 'react';
import { Message } from '../../types/chat';
import { UserMessage } from './UserMessage';
import { AiMessage } from './AiMessage';

interface ChatMessageListProps {
  messages: Message[];
  isStreaming?: boolean;
}

export function ChatMessageList({ messages, isStreaming }: ChatMessageListProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const prevLengthRef = useRef(0);

  // 새 메시지 추가 또는 스트리밍 시에만 하단 스크롤
  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;

    // 메시지가 추가되었거나 스트리밍 중일 때만 스크롤
    if (messages.length !== prevLengthRef.current || isStreaming) {
      requestAnimationFrame(() => {
        el.scrollTop = el.scrollHeight;
      });
    }
    prevLengthRef.current = messages.length;
  }, [messages.length, isStreaming]);

  // 히스토리 복원 시 (메시지 전체 교체) — 레이아웃 후 조용히 하단 이동
  useLayoutEffect(() => {
    const el = containerRef.current;
    if (el && messages.length > 0) {
      el.scrollTop = el.scrollHeight;
    }
  }, [messages]);

  return (
    <div ref={containerRef} className="flex-1 min-h-0 overflow-y-auto p-4 space-y-2 custom-scrollbar">
      {messages.map((msg, index) => {
        const isLast = index === messages.length - 1;
        if (msg.role === 'user') {
          return <UserMessage key={msg.id} content={msg.content} />;
        }
        return (
          <AiMessage
            key={msg.id}
            message={msg}
            messageIndex={index}
            isStreaming={isLast && isStreaming}
          />
        );
      })}
    </div>
  );
}
