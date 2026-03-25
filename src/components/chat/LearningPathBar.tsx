import React from 'react';
import { MapPin, ChevronRight } from 'lucide-react';
import { useChatStore } from '../../store/chatStore';
import { useNavigate } from 'react-router-dom';
import { Button } from '../ui/button';
import { Separator } from '../ui/separator';

export function LearningPathBar() {
  const { currentTurn, searchedTerms, mainSessionId } = useChatStore();
  const navigate = useNavigate();

  return (
    <div className="h-14 bg-white border-t border-slate-200/60 flex items-center justify-between px-4 lg:px-6 shrink-0 shadow-[0_-4px_10px_-4px_rgba(0,0,0,0.05)] z-20">
      <div className="flex items-center gap-3 lg:gap-4">
        <div className="flex items-center gap-2 text-[15px] font-medium text-slate-700">
          <div className="p-1.5 bg-emerald-50 rounded-lg border border-emerald-100">
            <MapPin className="w-4 h-4 text-emerald-600" />
          </div>
          <span className="hidden sm:inline">{currentTurn}턴 진행중</span>
          <span className="sm:hidden">{currentTurn}턴</span>
        </div>
        
        {searchedTerms.length > 0 && (
          <div className="hidden md:flex items-center gap-3">
            <Separator orientation="vertical" className="h-5 bg-slate-200" />
            <div className="flex gap-2">
              {searchedTerms.map((term, idx) => (
                <span key={idx} className="px-2.5 py-1 rounded-full bg-slate-50 text-xs font-medium text-slate-600 border border-slate-200">
                  {term}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>

      <Button 
        variant="ghost"
        size="sm"
        onClick={() => navigate(`/learning-path/${mainSessionId || 'session-1'}`)}
        className="text-blue-600 hover:text-blue-700 hover:bg-blue-50 gap-1 rounded-full px-4"
      >
        <span className="font-semibold">학습 경로 보기</span>
        <ChevronRight className="w-4 h-4" />
      </Button>
    </div>
  );
}
