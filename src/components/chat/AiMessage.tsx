import React from 'react';
import ReactMarkdown from 'react-markdown';
import { FaqHitCards } from './FaqHitCards';
import { FeedbackButtons } from './FeedbackButtons';
import { Bot } from 'lucide-react';
import { Message } from '../../types/chat';
import { Avatar, AvatarFallback } from '../ui/avatar';

interface AiMessageProps {
  message: Message;
  messageIndex: number;
  isStreaming?: boolean;
}

export function AiMessage({ message, messageIndex, isStreaming }: AiMessageProps) {
  const hasFaqHits = message.faqHits && message.faqHits.length > 0;

  return (
    <div className="flex gap-3 lg:gap-4 mb-6 max-w-[95%] lg:max-w-[90%]">
      <Avatar className="w-8 h-8 lg:w-10 lg:h-10 border border-white bg-blue-50 shadow-sm shrink-0 mt-1">
        <AvatarFallback className="bg-transparent">
          <Bot className="w-4 h-4 lg:w-5 lg:h-5 text-blue-600" />
        </AvatarFallback>
      </Avatar>

      <div className="flex flex-col items-start min-w-0 w-full">
        {/* FAQ 카드 — 답변 위에 표시 */}
        {hasFaqHits && (
          <div className="w-full mb-2">
            <FaqHitCards hits={message.faqHits!} />
          </div>
        )}

        <div className="bg-white text-slate-800 px-5 py-3.5 rounded-3xl rounded-tl-sm text-[15px] leading-relaxed shadow-sm border border-slate-100 prose prose-sm prose-slate max-w-none prose-p:my-1 prose-ul:my-1 prose-ol:my-1 prose-li:my-0.5 prose-pre:bg-slate-900 prose-pre:text-slate-100 prose-pre:rounded-xl prose-pre:text-sm prose-code:text-blue-600 prose-code:bg-blue-50 prose-code:px-1.5 prose-code:py-0.5 prose-code:rounded-md prose-code:before:content-none prose-code:after:content-none prose-headings:text-slate-800">
          <ReactMarkdown>{message.content}</ReactMarkdown>
          {isStreaming && (
            <span className="inline-block w-1.5 h-4 ml-1 bg-blue-500 animate-pulse align-middle" />
          )}
        </div>

        {!isStreaming && message.content && <FeedbackButtons messageIndex={messageIndex} />}
      </div>
    </div>
  );
}
