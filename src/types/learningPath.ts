export type LearningPathEvent = {
  id: string;
  eventType:
    | 'STUDENT_QUESTION'
    | 'SOCRATIC_QUESTION'
    | 'TERM_SEARCHED'
    | 'FAQ_HIT'
    | 'FAQ_MISS'
    | 'RESOLVED';
  turnNumber: number;
  content: string;
  faqHitId?: string;
  metadata?: { similarity?: number };
  createdAt: string;
};

export type LearningPathDetail = {
  sessionId: string;
  courseName: string;
  totalTurns: number;
  resolved: boolean;
  durationMinutes: number;
  events: LearningPathEvent[];
};
