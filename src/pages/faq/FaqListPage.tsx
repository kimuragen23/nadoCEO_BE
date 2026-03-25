import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getFaqs, FaqItem } from '../../api/client';
import { Button } from '../../components/ui/button';
import { Badge } from '../../components/ui/badge';
import { ArrowLeft, Sparkles, ThumbsUp, ChevronDown, ChevronUp } from 'lucide-react';

const DEFAULT_COURSE_ID = '00000000-0000-0000-0000-000000000001';

function FaqCard({ faq }: { faq: FaqItem }) {
  const [expanded, setExpanded] = useState(false);

  return (
    <div
      className="bg-white border border-slate-200/60 rounded-2xl p-5 hover:shadow-md transition-all cursor-pointer"
      onClick={() => setExpanded(!expanded)}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          <p className="text-sm font-bold text-slate-800 leading-snug">Q. {faq.question}</p>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          {faq.upvotes > 0 && (
            <Badge className="bg-blue-50 text-blue-600 border border-blue-100 shadow-none text-[10px] px-1.5 py-0">
              <ThumbsUp className="w-3 h-3 mr-1" />
              {faq.upvotes}
            </Badge>
          )}
          <Badge className="bg-slate-100 text-slate-500 shadow-none text-[10px] px-1.5 py-0">
            {faq.source === 'student' ? '학생' : '강사'}
          </Badge>
          {expanded ? <ChevronUp className="w-4 h-4 text-slate-400" /> : <ChevronDown className="w-4 h-4 text-slate-400" />}
        </div>
      </div>

      {expanded && (
        <div className="mt-4 pt-4 border-t border-slate-100">
          <p className="text-xs font-semibold text-slate-400 mb-1.5">A.</p>
          <p className="text-sm text-slate-700 leading-relaxed whitespace-pre-wrap">{faq.answer}</p>
        </div>
      )}

      {!expanded && (
        <p className="text-xs text-slate-400 mt-2 line-clamp-1">A. {faq.answer}</p>
      )}
    </div>
  );
}

export function FaqListPage() {
  const [faqs, setFaqs] = useState<FaqItem[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    getFaqs(DEFAULT_COURSE_ID)
      .then(setFaqs)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="min-h-screen bg-[#F4F5F0] font-sans">
      {/* Header */}
      <header className="bg-white border-b border-slate-200/60 px-6 py-4">
        <div className="max-w-3xl mx-auto flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => navigate(-1)} className="shrink-0">
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div className="flex items-center gap-3">
            <div className="p-2 bg-purple-100 rounded-xl">
              <Sparkles className="w-5 h-5 text-purple-600" />
            </div>
            <div>
              <h1 className="text-lg font-bold text-slate-800">FAQ 모음</h1>
              <p className="text-xs text-slate-500">코칭에서 축적된 질문과 답변</p>
            </div>
          </div>
          <Badge className="ml-auto bg-purple-50 text-purple-600 border border-purple-100 shadow-none">
            {faqs.length}개
          </Badge>
        </div>
      </header>

      {/* Content */}
      <main className="max-w-3xl mx-auto px-6 py-6">
        {loading ? (
          <p className="text-center text-slate-400 py-20">로딩 중...</p>
        ) : faqs.length === 0 ? (
          <div className="text-center py-20">
            <Sparkles className="w-12 h-12 text-slate-300 mx-auto mb-4" />
            <p className="text-slate-500 font-medium">아직 FAQ가 없습니다</p>
            <p className="text-sm text-slate-400 mt-1">코칭에서 "도움됨"이나 "해결완료"를 누르면 FAQ로 저장됩니다</p>
          </div>
        ) : (
          <div className="space-y-3">
            {faqs.map((faq) => (
              <FaqCard key={faq.id} faq={faq} />
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
