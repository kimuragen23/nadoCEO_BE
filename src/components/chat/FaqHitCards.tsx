import React, { useState } from 'react';
import { Sparkles, ChevronDown, ChevronUp } from 'lucide-react';
import { FaqHit } from '../../types/chat';
import { Badge } from '../ui/badge';

interface FaqHitCardsProps {
  hits: FaqHit[];
}

function FaqCard({ hit }: { hit: FaqHit }) {
  const [expanded, setExpanded] = useState(false);

  return (
    <div
      className="bg-purple-50 border border-purple-200/60 rounded-xl p-3.5 hover:bg-purple-100/50 transition-colors cursor-pointer"
      onClick={() => setExpanded(!expanded)}
    >
      <div className="flex items-start justify-between gap-2">
        <div className="flex items-center gap-2 min-w-0">
          <p className="text-sm font-semibold text-purple-900 leading-snug">Q. {hit.question}</p>
        </div>
        <div className="flex items-center gap-1.5 shrink-0">
          <Badge className="bg-purple-100 text-purple-700 hover:bg-purple-200 text-[10px] px-1.5 py-0 border border-purple-200/50 shadow-none">
            {Math.round(hit.similarity * 100)}%
          </Badge>
          {hit.answer && (
            expanded
              ? <ChevronUp className="w-3.5 h-3.5 text-purple-400" />
              : <ChevronDown className="w-3.5 h-3.5 text-purple-400" />
          )}
        </div>
      </div>
      {hit.answer && expanded && (
        <div className="mt-2.5 pt-2.5 border-t border-purple-200/50">
          <p className="text-xs font-medium text-purple-500 mb-1">A.</p>
          <p className="text-sm text-purple-800 leading-relaxed whitespace-pre-wrap">{hit.answer}</p>
        </div>
      )}
      {hit.answer && !expanded && (
        <p className="text-xs text-purple-500 mt-1.5">답변 보기 클릭</p>
      )}
    </div>
  );
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
          <FaqCard key={hit.faqId} hit={hit} />
        ))}
      </div>
    </div>
  );
}
