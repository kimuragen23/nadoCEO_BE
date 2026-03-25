import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getGlossary, GlossaryTerm } from '../../api/client';
import { useAuthStore } from '../../store/authStore';
import { Button } from '../../components/ui/button';
import { Badge } from '../../components/ui/badge';
import { ArrowLeft, BookOpen, Search } from 'lucide-react';
import { Input } from '../../components/ui/input';

export function GlossaryPage() {
  const [terms, setTerms] = useState<GlossaryTerm[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const { userId } = useAuthStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (!userId) return;
    getGlossary(userId)
      .then(setTerms)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [userId]);

  const filtered = search
    ? terms.filter((t) => t.term.toLowerCase().includes(search.toLowerCase()))
    : terms;

  const formatDate = (dateStr: string) => {
    try {
      return new Date(dateStr).toLocaleDateString('ko-KR', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return '';
    }
  };

  return (
    <div className="min-h-screen bg-[#F4F5F0] font-sans">
      {/* Header */}
      <header className="bg-white border-b border-slate-200/60 px-6 py-4">
        <div className="max-w-3xl mx-auto flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => navigate(-1)} className="shrink-0">
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div className="flex items-center gap-3">
            <div className="p-2 bg-amber-100 rounded-xl">
              <BookOpen className="w-5 h-5 text-amber-600" />
            </div>
            <div>
              <h1 className="text-lg font-bold text-slate-800">용어집</h1>
              <p className="text-xs text-slate-500">내가 검색한 용어 모음</p>
            </div>
          </div>
          <Badge className="ml-auto bg-amber-50 text-amber-600 border border-amber-100 shadow-none">
            {terms.length}개
          </Badge>
        </div>
      </header>

      {/* Search */}
      <div className="max-w-3xl mx-auto px-6 pt-6">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <Input
            placeholder="용어 검색..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-10 h-11 rounded-xl border-slate-200 bg-white text-slate-900"
          />
        </div>
      </div>

      {/* Content */}
      <main className="max-w-3xl mx-auto px-6 py-4">
        {loading ? (
          <p className="text-center text-slate-400 py-20">로딩 중...</p>
        ) : filtered.length === 0 ? (
          <div className="text-center py-20">
            <BookOpen className="w-12 h-12 text-slate-300 mx-auto mb-4" />
            <p className="text-slate-500 font-medium">
              {search ? '검색 결과가 없습니다' : '아직 검색한 용어가 없습니다'}
            </p>
            <p className="text-sm text-slate-400 mt-1">오른쪽 용어 채팅에서 검색하면 자동으로 저장됩니다</p>
          </div>
        ) : (
          <div className="grid gap-2">
            {filtered.map((t, i) => (
              <div
                key={i}
                className="bg-white border border-slate-200/60 rounded-xl px-5 py-3.5 flex items-center justify-between hover:shadow-sm transition-all"
              >
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 bg-amber-50 rounded-lg flex items-center justify-center shrink-0">
                    <span className="text-amber-600 font-bold text-sm">
                      {t.term.charAt(0).toUpperCase()}
                    </span>
                  </div>
                  <span className="text-sm font-semibold text-slate-800">{t.term}</span>
                </div>
                <span className="text-xs text-slate-400">{formatDate(t.lastSearchedAt)}</span>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
