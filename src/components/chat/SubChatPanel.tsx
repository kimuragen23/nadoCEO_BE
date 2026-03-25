import React from 'react';
import { Search } from 'lucide-react';
import { useChatStore } from '../../store/chatStore';
import { useChat } from '../../hooks/useChat';
import { ChatMessageList } from './ChatMessageList';
import { ChatInput } from './ChatInput';

export function SubChatPanel() {
  const { subMessages, searchedTerms } = useChatStore();
  const { input, setInput, sendMessage } = useChat('sub');

  return (
    <div className="flex flex-col h-full min-h-0 overflow-hidden bg-white">
      <div className="px-4 lg:px-6 py-3 lg:py-4 border-b border-slate-200/60 bg-white/80 backdrop-blur-md flex items-center justify-between shadow-sm z-10 shrink-0">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-amber-100 rounded-xl border border-amber-200">
            <Search className="w-4 h-4 text-amber-600" />
          </div>
          <h2 className="text-[15px] font-semibold text-slate-800">용어 빠른 질문</h2>
        </div>
        {searchedTerms.length > 0 && (
          <div className="hidden lg:flex items-center gap-2 text-xs text-slate-500 bg-slate-50 px-3 py-1.5 rounded-full border border-slate-200">
            <span className="font-medium">최근 검색</span>
            <div className="w-px h-3 bg-slate-300" />
            <div className="flex gap-1.5">
              {searchedTerms.slice(-2).map((term, i) => (
                <span key={i} className="text-amber-600 font-medium">{term}</span>
              ))}
            </div>
          </div>
        )}
      </div>
      
      <ChatMessageList messages={subMessages} />
      
      <ChatInput 
        value={input}
        onChange={setInput}
        onSend={() => sendMessage(input)}
        placeholder="궁금한 용어를 입력하세요..."
        theme="sub"
      />
    </div>
  );
}
