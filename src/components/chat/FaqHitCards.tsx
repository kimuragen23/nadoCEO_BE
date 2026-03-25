import React from 'react';
import { Sparkles } from 'lucide-react';
import { FaqHit } from '../../types/chat';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Badge } from '../ui/badge';

interface FaqHitCardsProps {
  hits: FaqHit[];
}

export function FaqHitCards({ hits }: FaqHitCardsProps) {
  if (!hits || hits.length === 0) return null;

  return (
    <div className="mt-4 w-full">
      <div className="flex items-center gap-1.5 mb-2 text-xs font-medium text-blue-600 ml-1">
        <Sparkles className="w-3.5 h-3.5" />
        <span>FAQ 추천 답변</span>
      </div>
      <div className="flex gap-3 overflow-x-auto pb-2 custom-scrollbar snap-x">
        {hits.map((hit) => (
          <Card 
            key={hit.faqId} 
            className="min-w-[240px] max-w-[280px] shrink-0 bg-white border-slate-200/60 shadow-sm hover:shadow-md hover:border-blue-200 transition-all cursor-pointer snap-start flex flex-col rounded-2xl"
          >
            <CardHeader className="p-3.5 pb-2 flex flex-row items-start justify-between gap-2 space-y-0">
              <CardTitle className="text-sm font-semibold leading-tight text-slate-800 line-clamp-2">
                {hit.question}
              </CardTitle>
              <Badge variant="secondary" className="bg-blue-50 text-blue-600 hover:bg-blue-100 text-[10px] px-1.5 py-0 shrink-0 border border-blue-100/50 shadow-none">
                {Math.round(hit.similarity * 100)}%
              </Badge>
            </CardHeader>
            <CardContent className="p-3.5 pt-0 flex-1">
              {hit.snippet && (
                <p className="text-[13px] text-slate-500 line-clamp-2 leading-relaxed">
                  {hit.snippet}
                </p>
              )}
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
