import React, { KeyboardEvent } from 'react';
import { Send } from 'lucide-react';
import { cn } from '../../lib/utils';
import { Input } from '../ui/input';
import { Button } from '../ui/button';

interface ChatInputProps {
  value: string;
  onChange: (val: string) => void;
  onSend: () => void;
  placeholder?: string;
  disabled?: boolean;
  theme?: 'main' | 'sub';
}

export function ChatInput({ value, onChange, onSend, placeholder = '메시지 입력...', disabled, theme = 'main' }: ChatInputProps) {
  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      onSend();
    }
  };

  const isMain = theme === 'main';

  return (
    <div className={cn(
      "p-3 lg:p-4 border-t backdrop-blur-sm shrink-0",
      isMain ? "bg-[#F4F5F0]/80 border-slate-200/60" : "bg-white/80 border-slate-200/60"
    )}>
      <div className="relative flex items-center max-w-4xl mx-auto">
        <Input
          type="text"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={placeholder}
          disabled={disabled}
          className="w-full bg-white border-slate-200 text-slate-900 text-[15px] rounded-full pl-5 pr-14 py-6 focus-visible:ring-1 focus-visible:ring-blue-500 focus-visible:border-blue-500 shadow-sm transition-all"
        />
        <Button
          size="icon"
          onClick={onSend}
          disabled={disabled || !value.trim()}
          className={cn(
            "absolute right-1.5 h-10 w-10 rounded-full transition-all shadow-sm",
            isMain 
              ? "bg-blue-600 hover:bg-blue-700 text-white" 
              : "bg-amber-500 hover:bg-amber-600 text-white"
          )}
        >
          <Send className="w-4 h-4 ml-0.5" />
        </Button>
      </div>
    </div>
  );
}
