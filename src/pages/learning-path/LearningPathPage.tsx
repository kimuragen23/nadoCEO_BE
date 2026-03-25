import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { PathHeader } from '../../components/learning-path/PathHeader';
import { TurnTimeline } from '../../components/learning-path/TurnTimeline';
import { LearningPathDetail, LearningPathEvent } from '../../types/learningPath';
import { getSessionTimeline, TimelineEvent } from '../../api/client';

export function LearningPathPage() {
  const { sessionId } = useParams<{ sessionId: string }>();
  const [detail, setDetail] = useState<LearningPathDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!sessionId) return;

    setLoading(true);
    getSessionTimeline(sessionId)
      .then((events) => {
        const mapped: LearningPathEvent[] = events.map((e: TimelineEvent) => ({
          id: e.id,
          eventType: e.eventType as LearningPathEvent['eventType'],
          turnNumber: e.turnNumber,
          content: e.content || '',
          faqHitId: e.faqHitId,
          metadata: e.metadata ? JSON.parse(e.metadata) : undefined,
          createdAt: e.createdAt,
        }));

        const maxTurn = Math.max(...mapped.map((e) => e.turnNumber), 0);
        const hasResolved = mapped.some((e) => e.eventType === 'RESOLVED');

        setDetail({
          sessionId,
          courseName: '',
          totalTurns: maxTurn,
          resolved: hasResolved,
          durationMinutes: 0,
          events: mapped,
        });
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [sessionId]);

  if (loading) {
    return (
      <div className="min-h-screen bg-[#F4F5F0] flex items-center justify-center">
        <p className="text-slate-500">로딩 중...</p>
      </div>
    );
  }

  if (error || !detail) {
    return (
      <div className="min-h-screen bg-[#F4F5F0] flex items-center justify-center">
        <p className="text-red-500">{error || '데이터를 불러올 수 없습니다.'}</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#F4F5F0] font-sans text-slate-900">
      <PathHeader detail={detail} />
      <TurnTimeline detail={detail} />
    </div>
  );
}
