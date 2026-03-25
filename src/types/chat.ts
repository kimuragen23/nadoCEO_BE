export type FaqHit = {
  faqId: string;
  similarity: number; // 0~1
  question: string;
  answer?: string;
  snippet?: string;
};

export type Message = {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  faqHits?: FaqHit[];
  createdAt: string;
};
