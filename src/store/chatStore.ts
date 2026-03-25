import { create } from 'zustand';
import { Message, FaqHit } from '../types/chat';

interface ChatStore {
  mainSessionId: string | null;
  mainMessages: Message[];
  mainIsStreaming: boolean;
  subSessionId: string | null;
  subMessages: Message[];
  subIsStreaming: boolean;
  currentTurn: number;
  searchedTerms: string[];

  appendMainMessage: (msg: Message) => void;
  appendStreamChunk: (chunk: string) => void;
  addFaqHitToLastMessage: (faq: FaqHit) => void;
  appendSubMessage: (msg: Message) => void;
  appendSubStreamChunk: (chunk: string) => void;
  addSearchedTerm: (term: string) => void;
  setStreaming: (v: boolean) => void;
  setSubStreaming: (v: boolean) => void;
  setMainSessionId: (id: string) => void;
  setSubSessionId: (id: string) => void;
  incrementTurn: () => void;
  resetSession: () => void;
  restoreSession: (sessionId: string, messages: Message[], totalTurns: number) => void;
}

export const useChatStore = create<ChatStore>((set) => ({
  mainSessionId: null,
  mainMessages: [],
  mainIsStreaming: false,
  subSessionId: null,
  subMessages: [],
  subIsStreaming: false,
  currentTurn: 0,
  searchedTerms: [],

  appendMainMessage: (msg) =>
    set((state) => ({ mainMessages: [...state.mainMessages, msg] })),

  appendStreamChunk: (chunk) =>
    set((state) => {
      const lastMsg = state.mainMessages[state.mainMessages.length - 1];
      if (!lastMsg || lastMsg.role !== 'assistant') return state;

      const updatedMessages = [...state.mainMessages];
      updatedMessages[updatedMessages.length - 1] = {
        ...lastMsg,
        content: lastMsg.content + chunk,
      };

      return { mainMessages: updatedMessages };
    }),

  addFaqHitToLastMessage: (faq) =>
    set((state) => {
      const lastMsg = state.mainMessages[state.mainMessages.length - 1];
      if (!lastMsg || lastMsg.role !== 'assistant') return state;
      const updatedMessages = [...state.mainMessages];
      updatedMessages[updatedMessages.length - 1] = {
        ...lastMsg,
        faqHits: [...(lastMsg.faqHits || []), faq],
      };
      return { mainMessages: updatedMessages };
    }),

  appendSubMessage: (msg) =>
    set((state) => ({ subMessages: [...state.subMessages, msg] })),

  appendSubStreamChunk: (chunk) =>
    set((state) => {
      const lastMsg = state.subMessages[state.subMessages.length - 1];
      if (!lastMsg || lastMsg.role !== 'assistant') return state;

      const updatedMessages = [...state.subMessages];
      updatedMessages[updatedMessages.length - 1] = {
        ...lastMsg,
        content: lastMsg.content + chunk,
      };

      return { subMessages: updatedMessages };
    }),

  addSearchedTerm: (term) =>
    set((state) => {
      if (state.searchedTerms.includes(term)) return state;
      return { searchedTerms: [...state.searchedTerms, term] };
    }),

  setStreaming: (v) => set({ mainIsStreaming: v }),
  setSubStreaming: (v) => set({ subIsStreaming: v }),
  setMainSessionId: (id) => set({ mainSessionId: id }),
  setSubSessionId: (id) => set({ subSessionId: id }),
  incrementTurn: () => set((state) => ({ currentTurn: state.currentTurn + 1 })),

  resetSession: () =>
    set({
      mainSessionId: null,
      mainMessages: [],
      mainIsStreaming: false,
      subSessionId: null,
      subMessages: [],
      subIsStreaming: false,
      currentTurn: 0,
      searchedTerms: [],
    }),

  restoreSession: (sessionId, messages, totalTurns) =>
    set({
      mainSessionId: sessionId,
      mainMessages: messages,
      mainIsStreaming: false,
      currentTurn: totalTurns,
    }),
}));
