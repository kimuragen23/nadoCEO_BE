import React, { useState } from 'react';
import { ThumbsUp, ThumbsDown, CheckCircle2 } from 'lucide-react';
import { cn } from '../../lib/utils';
import { Button } from '../ui/button';
import { useChatStore } from '../../store/chatStore';
import { apiFetch } from '../../api/client';

interface FeedbackButtonsProps {
  messageIndex: number;
}

export function FeedbackButtons({ messageIndex }: FeedbackButtonsProps) {
  const [feedback, setFeedback] = useState<'up' | 'down' | null>(null);
  const [isResolved, setIsResolved] = useState(false);
  const [loading, setLoading] = useState(false);
  const mainSessionId = useChatStore((s) => s.mainSessionId);

  const sendFeedback = async (type: 'up' | 'down' | 'resolved') => {
    if (!mainSessionId || loading) return;
    setLoading(true);
    try {
      await apiFetch('/chat/message-feedback', {
        method: 'POST',
        body: JSON.stringify({
          sessionId: mainSessionId,
          messageIndex,
          feedbackType: type,
        }),
      });
    } catch (err) {
      console.error('Feedback error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleUp = async () => {
    const next = feedback === 'up' ? null : 'up';
    setFeedback(next);
    if (next === 'up') await sendFeedback('up');
  };

  const handleDown = async () => {
    const next = feedback === 'down' ? null : 'down';
    setFeedback(next);
    if (next === 'down') await sendFeedback('down');
  };

  const handleResolved = async () => {
    if (isResolved) return;
    setIsResolved(true);
    await sendFeedback('resolved');
  };

  return (
    <div className="flex items-center gap-1.5 mt-3 ml-1">
      <Button
        variant="ghost"
        size="sm"
        onClick={handleUp}
        disabled={loading}
        className={cn(
          "h-8 px-3 rounded-full transition-all border border-transparent",
          feedback === 'up'
            ? "bg-blue-50 text-blue-700 border-blue-200 shadow-sm hover:bg-blue-100"
            : "text-slate-500 hover:text-slate-700 hover:bg-slate-100"
        )}
      >
        <ThumbsUp className={cn("w-4 h-4 mr-1.5", feedback === 'up' && "fill-blue-600/20")} />
        <span className="text-xs font-medium">도움됨</span>
      </Button>
      <Button
        variant="ghost"
        size="sm"
        onClick={handleDown}
        disabled={loading}
        className={cn(
          "h-8 px-3 rounded-full transition-all border border-transparent",
          feedback === 'down'
            ? "bg-red-50 text-red-700 border-red-200 shadow-sm hover:bg-red-100"
            : "text-slate-500 hover:text-slate-700 hover:bg-slate-100"
        )}
      >
        <ThumbsDown className={cn("w-4 h-4 mr-1.5", feedback === 'down' && "fill-red-600/20")} />
        <span className="text-xs font-medium">별로임</span>
      </Button>

      <div className="w-px h-4 bg-slate-200 mx-1" />

      <Button
        variant="ghost"
        size="sm"
        onClick={handleResolved}
        disabled={loading || isResolved}
        className={cn(
          "h-8 px-3 rounded-full transition-all border border-transparent",
          isResolved
            ? "bg-emerald-50 text-emerald-700 border-emerald-200 shadow-sm hover:bg-emerald-100"
            : "text-slate-500 hover:text-slate-700 hover:bg-slate-100"
        )}
      >
        <CheckCircle2 className={cn("w-4 h-4 mr-1.5", isResolved && "fill-emerald-600/20")} />
        <span className="text-xs font-medium">해결완료</span>
      </Button>
    </div>
  );
}
