import React from 'react';
import { Sparkles } from 'lucide-react';

interface FaqHitBadgeProps {
  similarity: number;
  question: string;
}

export function FaqHitBadge({ similarity, question }: FaqHitBadgeProps) {
  return (
    <div className="flex items-center gap-1.5 mt-2 text-xs font-medium text-purple-400 bg-purple-500/10 px-2.5 py-1.5 rounded-md w-fit border border-purple-500/20">
      <Sparkles className="w-3.5 h-3.5" />
      <span>FAQ HIT 유사도 {Math.round(similarity * 100)}%</span>
      <span className="text-purple-400/60 ml-1 truncate max-w-[200px]">— "{question}"</span>
    </div>
  );
}
