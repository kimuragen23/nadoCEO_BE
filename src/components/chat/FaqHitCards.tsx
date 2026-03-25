import React from 'react';
import { Sparkles } from 'lucide-react';
import { FaqHit } from '../../types/chat';
import { Badge } from '../ui/badge';

interface FaqHitCardsProps {
  hits: FaqHit[];
}

export function FaqHitCards({ hits }: FaqHitCardsProps) {
  if (!hits || hits.length === 0) return null;

  return (
    <div className="w-full">
      <div className="flex items-center gap-1.5 mb-2 text-xs font-medium text-purple-600 ml-1">
        <Sparkles className="w-3.5 h-3.5" />
        <span>유사한 FAQ가 발견되었습니다</span>
      </div>
      <div className="space-y-2">
        {hits.map((hit) => (
          <div
            key={hit.faqId}
            className="bg-purple-50 border border-purple-200/60 rounded-xl p-3.5 hover:bg-purple-100/50 transition-colors"
          >
            <div className="flex items-start justify-between gap-2">
              <p className="text-sm font-semibold text-purple-900 leading-snug">{hit.question}</p>
              <Badge className="bg-purple-100 text-purple-700 hover:bg-purple-200 text-[10px] px-1.5 py-0 shrink-0 border border-purple-200/50 shadow-none">
                {Math.round(hit.similarity * 100)}%
              </Badge>
            </div>
            {hit.snippet && (
              <p className="text-xs text-purple-700/70 mt-1.5 leading-relaxed line-clamp-2">{hit.snippet}</p>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
