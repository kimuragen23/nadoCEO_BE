import React from 'react';
import { BookOpen, CheckCircle2, Clock, Hash, ChevronLeft } from 'lucide-react';
import { LearningPathDetail } from '../../types/learningPath';
import { useNavigate } from 'react-router-dom';
import { Button } from '../ui/button';
import { Separator } from '../ui/separator';

interface PathHeaderProps {
  detail: LearningPathDetail;
}

export function PathHeader({ detail }: PathHeaderProps) {
  const navigate = useNavigate();
  
  return (
    <div className="bg-white border-b border-slate-200/60 px-4 lg:px-6 py-6 lg:py-8 shadow-sm">
      <div className="max-w-4xl mx-auto">
        <Button 
          variant="ghost" 
          size="sm" 
          onClick={() => navigate(-1)} 
          className="text-slate-500 hover:text-slate-800 hover:bg-slate-100 -ml-3 mb-4 gap-1 rounded-full px-4"
        >
          <ChevronLeft className="w-4 h-4" />
          코칭 채팅으로 돌아가기
        </Button>

        <div className="flex flex-wrap items-center gap-2 lg:gap-3 text-[14px] lg:text-[15px] text-slate-500 mb-6 font-medium">
          <span className="flex items-center gap-2 text-blue-600 bg-blue-50 px-2.5 py-1 rounded-md">
            <BookOpen className="w-4 h-4" />
            내 학습 경로
          </span>
          <ChevronRightIcon className="w-4 h-4 text-slate-400" />
          <span>{detail.courseName}</span>
          <ChevronRightIcon className="w-4 h-4 text-slate-400" />
          <span className="text-slate-800 font-semibold">NullPointerException 해결</span>
        </div>
        
        <div className="flex flex-wrap items-center gap-4 lg:gap-6">
          <div className="flex items-center gap-2 text-emerald-700 bg-emerald-50 px-4 py-2 rounded-full border border-emerald-200 shadow-sm">
            <CheckCircle2 className="w-5 h-5 text-emerald-600" />
            <span className="text-[15px] font-semibold">해결 완료</span>
          </div>
          
          <Separator orientation="vertical" className="h-8 bg-slate-200 hidden sm:block" />
          
          <div className="flex items-center gap-2.5 text-slate-700">
            <div className="p-1.5 bg-slate-50 rounded-lg border border-slate-200">
              <Hash className="w-4 h-4 text-slate-500" />
            </div>
            <span className="text-[15px] font-medium">총 {detail.totalTurns}턴</span>
          </div>
          
          <div className="flex items-center gap-2.5 text-slate-700">
            <div className="p-1.5 bg-slate-50 rounded-lg border border-slate-200">
              <Clock className="w-4 h-4 text-slate-500" />
            </div>
            <span className="text-[15px] font-medium">소요 {detail.durationMinutes}분</span>
          </div>
        </div>
      </div>
    </div>
  );
}

function ChevronRightIcon({ className }: { className?: string }) {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={className}>
      <path d="m9 18 6-6-6-6"/>
    </svg>
  );
}
