import React from 'react';
import { BookOpen } from 'lucide-react';
import { useChatStore } from '../../store/chatStore';
import { useChat } from '../../hooks/useChat';
import { ChatMessageList } from './ChatMessageList';
import { ChatInput } from './ChatInput';

export function MainChatPanel() {
  const { mainMessages, mainIsStreaming } = useChatStore();
  const { input, setInput, sendMessage } = useChat('main');

  return (
    <div className="flex flex-col h-full min-h-0 overflow-hidden bg-[#F4F5F0]">
      <div className="px-4 lg:px-6 py-3 lg:py-4 border-b border-slate-200/60 bg-[#F4F5F0]/80 backdrop-blur-md flex items-center gap-3 shadow-sm z-10 shrink-0">
        <div className="p-2 bg-blue-100 rounded-xl border border-blue-200">
          <BookOpen className="w-4 h-4 text-blue-600" />
        </div>
        <h2 className="text-[15px] font-semibold text-slate-800">메인 코칭 채팅</h2>
      </div>
      
      <ChatMessageList messages={mainMessages} isStreaming={mainIsStreaming} />
      
      <ChatInput 
        value={input}
        onChange={setInput}
        onSend={() => sendMessage(input)}
        disabled={mainIsStreaming}
        theme="main"
      />
    </div>
  );
}
