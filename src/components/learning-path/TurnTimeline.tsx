import React from 'react';
import { LearningPathDetail, LearningPathEvent } from '../../types/learningPath';
import { MessageSquare, BrainCircuit, Search, Sparkles, CheckCircle2 } from 'lucide-react';
import { Card, CardContent } from '../ui/card';
import { Badge } from '../ui/badge';

interface TurnTimelineProps {
  detail: LearningPathDetail;
}

export function TurnTimeline({ detail }: TurnTimelineProps) {
  // Group events by turnNumber
  const turns = detail.events.reduce((acc, event) => {
    if (!acc[event.turnNumber]) {
      acc[event.turnNumber] = [];
    }
    acc[event.turnNumber].push(event);
    return acc;
  }, {} as Record<number, LearningPathEvent[]>);

  return (
    <div className="max-w-4xl mx-auto py-8 lg:py-10 px-4 lg:px-6">
      <div className="space-y-10 lg:space-y-12">
        {Object.entries(turns).map(([turnNumber, events]) => (
          <div key={turnNumber} className="relative">
            {/* Turn Header */}
            <div className="flex items-center gap-4 mb-6">
              <Badge variant="outline" className="text-sm font-bold text-slate-600 border-slate-200 bg-white px-3 py-1 shadow-sm rounded-full">
                Turn {turnNumber}
              </Badge>
              <div className="h-px bg-slate-200 flex-1" />
            </div>

            {/* Events */}
            <div className="pl-6 space-y-6 relative before:absolute before:inset-y-0 before:left-[11px] before:w-px before:bg-slate-200">
              {events.map((event) => (
                <EventItem key={event.id} event={event} />
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function EventItem({ event }: { event: LearningPathEvent }) {
  switch (event.eventType) {
    case 'STUDENT_QUESTION':
      return (
        <div className="relative flex gap-4 items-start">
          <div className="absolute -left-[21px] top-1 w-2.5 h-2.5 rounded-full bg-blue-600 ring-4 ring-[#F4F5F0]" />
          <MessageSquare className="w-5 h-5 text-blue-600 shrink-0 mt-0.5" />
          <Card className="flex-1 bg-white border-slate-200 shadow-sm rounded-2xl">
            <CardContent className="p-4 lg:p-5">
              <div className="text-xs font-semibold text-slate-500 mb-1.5">내 질문</div>
              <div className="text-slate-800 text-[15px] leading-relaxed">{event.content}</div>
            </CardContent>
          </Card>
        </div>
      );
    case 'SOCRATIC_QUESTION':
      return (
        <div className="relative flex gap-4 items-start">
          <div className="absolute -left-[21px] top-1 w-2.5 h-2.5 rounded-full bg-indigo-500 ring-4 ring-[#F4F5F0]" />
          <BrainCircuit className="w-5 h-5 text-indigo-500 shrink-0 mt-0.5" />
          <Card className="flex-1 bg-indigo-50 border-indigo-100 shadow-sm rounded-2xl">
            <CardContent className="p-4 lg:p-5">
              <div className="text-xs font-semibold text-indigo-600/80 mb-1.5">AI 질문</div>
              <div className="text-indigo-950 text-[15px] leading-relaxed">{event.content}</div>
            </CardContent>
          </Card>
        </div>
      );
    case 'TERM_SEARCHED':
      return (
        <div className="relative flex gap-4 items-start">
          <div className="absolute -left-[21px] top-1 w-2.5 h-2.5 rounded-full bg-amber-500 ring-4 ring-[#F4F5F0]" />
          <Search className="w-5 h-5 text-amber-500 shrink-0 mt-0.5" />
          <div className="flex-1 pt-0.5">
            <div className="text-xs font-semibold text-slate-500 mb-2">검색한 용어</div>
            <Badge variant="secondary" className="bg-amber-50 text-amber-700 hover:bg-amber-100 border border-amber-200 px-3 py-1 text-sm rounded-full shadow-sm">
              {event.content}
            </Badge>
          </div>
        </div>
      );
    case 'FAQ_HIT':
      return (
        <div className="relative flex gap-4 items-start">
          <div className="absolute -left-[21px] top-1 w-2.5 h-2.5 rounded-full bg-purple-500 ring-4 ring-[#F4F5F0]" />
          <Sparkles className="w-5 h-5 text-purple-500 shrink-0 mt-0.5" />
          <div className="flex-1 pt-0.5">
            <div className="text-xs font-semibold text-slate-500 mb-1.5">FAQ 매칭</div>
            <div className="flex flex-wrap items-center gap-2">
              <span className="text-[15px] text-purple-900 font-medium">"{event.content}"</span>
              <Badge variant="outline" className="border-purple-200 text-purple-700 bg-purple-50 rounded-full">
                유사도 {Math.round((event.metadata?.similarity || 0) * 100)}%
              </Badge>
            </div>
          </div>
        </div>
      );
    case 'RESOLVED':
      return (
        <div className="relative flex gap-4 items-start mt-8">
          <div className="absolute -left-[21px] top-1 w-2.5 h-2.5 rounded-full bg-emerald-500 ring-4 ring-[#F4F5F0]" />
          <CheckCircle2 className="w-6 h-6 text-emerald-600 shrink-0" />
          <Card className="flex-1 bg-emerald-50 border-emerald-200 shadow-sm rounded-2xl">
            <CardContent className="p-5">
              <div className="text-emerald-700 font-bold text-lg mb-1">해결 완료!</div>
              <div className="text-emerald-800/80 text-[15px]">{event.content}</div>
            </CardContent>
          </Card>
        </div>
      );
    default:
      return null;
  }
}
