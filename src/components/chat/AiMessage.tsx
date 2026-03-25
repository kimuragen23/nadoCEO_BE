import React from 'react';
import { FaqHitCards } from './FaqHitCards';
import { FeedbackButtons } from './FeedbackButtons';
import { Bot } from 'lucide-react';
import { Message } from '../../types/chat';
import { Avatar, AvatarFallback } from '../ui/avatar';

interface AiMessageProps {
  message: Message;
  isStreaming?: boolean;
}

export function AiMessage({ message, isStreaming }: AiMessageProps) {
  return (
    <div className="flex gap-3 lg:gap-4 mb-6 max-w-[95%] lg:max-w-[90%]">
      <Avatar className="w-8 h-8 lg:w-10 lg:h-10 border border-white bg-blue-50 shadow-sm shrink-0 mt-1">
        <AvatarFallback className="bg-transparent">
          <Bot className="w-4 h-4 lg:w-5 lg:h-5 text-blue-600" />
        </AvatarFallback>
      </Avatar>
      
      <div className="flex flex-col items-start min-w-0 flex-1">
        <div className="bg-white text-slate-800 px-5 py-3.5 rounded-3xl rounded-tl-sm text-[15px] leading-relaxed shadow-sm border border-slate-100">
          {message.content}
          {isStreaming && (
            <span className="inline-block w-1.5 h-4 ml-1 bg-blue-500 animate-pulse align-middle" />
          )}
        </div>
        
        {message.faqHits && message.faqHits.length > 0 && (
          <FaqHitCards hits={message.faqHits} />
        )}
        
        {!isStreaming && <FeedbackButtons />}
      </div>
    </div>
  );
}
