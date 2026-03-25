import React, { useEffect, useRef } from 'react';
import { Message } from '../../types/chat';
import { UserMessage } from './UserMessage';
import { AiMessage } from './AiMessage';

interface ChatMessageListProps {
  messages: Message[];
  isStreaming?: boolean;
}

export function ChatMessageList({ messages, isStreaming }: ChatMessageListProps) {
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const el = containerRef.current;
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  }, [messages, isStreaming]);

  return (
    <div ref={containerRef} className="flex-1 overflow-y-auto p-4 space-y-2 custom-scrollbar">
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
