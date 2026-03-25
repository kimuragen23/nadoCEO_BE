import React, { useEffect, useState } from 'react';
import { Plus, History, Settings, HelpCircle, MessageSquare, ChevronLeft, ChevronRight } from 'lucide-react';
import { cn } from '../../lib/utils';
import { Button } from '../ui/button';
import { ScrollArea } from '../ui/scroll-area';
import { Separator } from '../ui/separator';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '../ui/tooltip';
import { useChatStore } from '../../store/chatStore';
import { useAuthStore } from '../../store/authStore';
import { getSessionHistory, getSession, SessionSummary } from '../../api/client';
import { Message } from '../../types/chat';

interface SidebarProps {
  isCollapsed: boolean;
  onToggle: () => void;
}

export function Sidebar({ isCollapsed, onToggle }: SidebarProps) {
  const [history, setHistory] = useState<SessionSummary[]>([]);
  const resetSession = useChatStore((s) => s.resetSession);
  const restoreSession = useChatStore((s) => s.restoreSession);
  const mainSessionId = useChatStore((s) => s.mainSessionId);
  const mainIsStreaming = useChatStore((s) => s.mainIsStreaming);
  const { userId } = useAuthStore();

  const loadHistory = () => {
    if (!userId) return;
    getSessionHistory(userId)
      .then(setHistory)
      .catch((err) => console.error('Failed to load history:', err));
  };

  // 초기 로드
  useEffect(() => { loadHistory(); }, [userId]);

  // 스트리밍 완료 시 히스토리 갱신 (새 세션이 생겼을 수 있으므로)
  useEffect(() => {
    if (!mainIsStreaming) loadHistory();
  }, [mainIsStreaming]);

  const handleNewSession = () => {
    resetSession();
  };

  const handleSelectSession = async (sessionId: string, e?: React.MouseEvent) => {
    // 버튼 포커스가 페이지 스크롤을 유발하므로 해제
    (e?.currentTarget as HTMLElement)?.blur();
    try {
      const session = await getSession(sessionId);
      const raw: Array<{ role: string; content: string; faqHits?: any[] }> = JSON.parse(session.messages || '[]');
      const messages: Message[] = raw.map((m, i) => ({
        id: `${sessionId}-${i}`,
        role: m.role as 'user' | 'assistant',
        content: m.content,
        faqHits: m.faqHits,
        createdAt: session.createdAt,
      }));
      restoreSession(sessionId, messages, session.totalTurns);
    } catch (err) {
      console.error('Failed to load session:', err);
    }
  };

  const formatDate = (dateStr: string) => {
    try {
      return new Date(dateStr).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
    } catch {
      return '';
    }
  };

  return (
    <aside
      className={cn(
        "flex flex-col bg-[#F4F5F0] border-r border-slate-200/60 transition-all duration-300 ease-in-out shrink-0 z-30",
        isCollapsed ? "w-16" : "w-64"
      )}
    >
      {/* Sidebar Header */}
      <div className="h-16 flex items-center justify-between px-4 border-b border-slate-200/60 shrink-0">
        {!isCollapsed && (
          <h1 className="text-lg font-bold tracking-tight text-slate-800 flex items-center gap-2">
            <span className="bg-blue-600 text-white p-1 rounded-md text-xs">AI</span>
            NADOCEO
          </h1>
        )}
        <Button
          variant="ghost"
          size="icon"
          onClick={onToggle}
          className="h-8 w-8 text-slate-500 hover:bg-slate-200/50"
        >
          {isCollapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
        </Button>
      </div>

      {/* New Chat Button */}
      <div className="p-3">
        {isCollapsed ? (
          <Tooltip>
            <TooltipTrigger
              render={
                <Button
                  variant="default"
                  size="icon"
                  onClick={handleNewSession}
                  className="w-10 h-10 rounded-xl bg-blue-600 hover:bg-blue-700 shadow-sm"
                />
              }
            >
              <Plus className="w-5 h-5" />
            </TooltipTrigger>
            <TooltipContent side="right">새 코칭 시작</TooltipContent>
          </Tooltip>
        ) : (
          <Button
            variant="default"
            onClick={handleNewSession}
            className="w-full justify-start gap-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl shadow-sm h-11 px-4"
          >
            <Plus className="w-4 h-4" />
            <span className="font-semibold">새 코칭 시작</span>
          </Button>
        )}
      </div>

      {/* History Section */}
      <div className="flex-1 flex flex-col min-h-0">
        <div className={cn(
          "px-4 py-3 text-[11px] font-bold text-slate-400 uppercase tracking-wider flex items-center gap-2",
          isCollapsed && "justify-center px-0"
        )}>
          <History className="w-3.5 h-3.5" />
          {!isCollapsed && <span>코칭 히스토리</span>}
        </div>
        <ScrollArea className="flex-1 px-2">
          <div className="space-y-1">
            {history.length === 0 && !isCollapsed && (
              <p className="text-xs text-slate-400 px-3 py-2">아직 히스토리가 없습니다</p>
            )}
            {history.map((item) => (
              <Tooltip key={item.id}>
                <TooltipTrigger
                  render={
                    <Button
                      variant="ghost"
                      onClick={(e) => handleSelectSession(item.id, e)}
                      className={cn(
                        "w-full justify-start gap-3 h-11 rounded-xl text-slate-600 hover:bg-white hover:text-blue-600 transition-all group border border-transparent hover:border-slate-200/60",
                        isCollapsed ? "px-0 justify-center" : "px-3"
                      )}
                    />
                  }
                >
                  <MessageSquare className={cn("w-4 h-4 shrink-0", isCollapsed ? "text-slate-400" : "text-slate-400 group-hover:text-blue-500")} />
                  {!isCollapsed && (
                    <div className="flex flex-col items-start overflow-hidden text-left">
                      <span className="text-sm font-semibold truncate w-full leading-tight">{item.title}</span>
                      <span className="text-[10px] text-slate-400 mt-0.5">
                        {formatDate(item.createdAt)} · {item.totalTurns}턴 {item.resolved ? '✓' : ''}
                      </span>
                    </div>
                  )}
                </TooltipTrigger>
                {isCollapsed && <TooltipContent side="right">{item.title}</TooltipContent>}
              </Tooltip>
            ))}
          </div>
        </ScrollArea>
      </div>

      {/* Bottom Actions */}
      <div className="p-3 border-t border-slate-200/60 space-y-1">
        <Tooltip>
          <TooltipTrigger
            render={
              <Button variant="ghost" className={cn("w-full justify-start gap-3 h-10 rounded-xl text-slate-600 hover:bg-white", isCollapsed && "px-0 justify-center")} />
            }
          >
            <HelpCircle className="w-4 h-4 text-slate-400" />
            {!isCollapsed && <span className="text-sm font-medium">도움말</span>}
          </TooltipTrigger>
          {isCollapsed && <TooltipContent side="right">도움말</TooltipContent>}
        </Tooltip>

        <Tooltip>
          <TooltipTrigger
            render={
              <Button variant="ghost" className={cn("w-full justify-start gap-3 h-10 rounded-xl text-slate-600 hover:bg-white", isCollapsed && "px-0 justify-center")} />
            }
          >
            <Settings className="w-4 h-4 text-slate-400" />
            {!isCollapsed && <span className="text-sm font-medium">설정</span>}
          </TooltipTrigger>
          {isCollapsed && <TooltipContent side="right">설정</TooltipContent>}
        </Tooltip>
      </div>
    </aside>
  );
}
