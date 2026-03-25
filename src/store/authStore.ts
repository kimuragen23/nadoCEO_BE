import { create } from 'zustand';

interface AuthStore {
  authenticated: boolean;
  userId: string | null;
  userName: string | null;
  userRole: string | null;
  token: string | null;
  refreshToken: string | null;

  setAuth: (data: {
    authenticated: boolean;
    userId: string | null;
    userName: string | null;
    userRole: string | null;
    token: string | null;
    refreshToken?: string | null;
  }) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthStore>((set) => ({
  authenticated: false,
  userId: null,
  userName: null,
  userRole: null,
  token: null,
  refreshToken: null,

  setAuth: (data) => set({ ...data, refreshToken: data.refreshToken ?? null }),
  clearAuth: () =>
    set({
      authenticated: false,
      userId: null,
      userName: null,
      userRole: null,
      token: null,
      refreshToken: null,
    }),
}));
