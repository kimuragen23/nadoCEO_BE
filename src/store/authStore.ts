import { create } from 'zustand';

const STORAGE_KEY = 'nadoceo_auth';

interface AuthState {
  authenticated: boolean;
  userId: string | null;
  userName: string | null;
  userRole: string | null;
  token: string | null;
  refreshToken: string | null;
}

interface AuthStore extends AuthState {
  setAuth: (data: Omit<AuthState, 'refreshToken'> & { refreshToken?: string | null }) => void;
  clearAuth: () => void;
}

function loadFromStorage(): AuthState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return JSON.parse(raw);
  } catch {}
  return { authenticated: false, userId: null, userName: null, userRole: null, token: null, refreshToken: null };
}

function saveToStorage(state: AuthState) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch {}
}

const initial = loadFromStorage();

export const useAuthStore = create<AuthStore>((set) => ({
  ...initial,

  setAuth: (data) => {
    const state: AuthState = { ...data, refreshToken: data.refreshToken ?? null };
    saveToStorage(state);
    set(state);
  },

  clearAuth: () => {
    localStorage.removeItem(STORAGE_KEY);
    set({
      authenticated: false,
      userId: null,
      userName: null,
      userRole: null,
      token: null,
      refreshToken: null,
    });
  },
}));
