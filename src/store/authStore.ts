import { create } from 'zustand';

interface AuthStore {
  authenticated: boolean;
  userId: string | null;
  userName: string | null;
  userRole: string | null;
  token: string | null;

  setAuth: (data: {
    authenticated: boolean;
    userId: string | null;
    userName: string | null;
    userRole: string | null;
    token: string | null;
  }) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthStore>((set) => ({
  authenticated: false,
  userId: null,
  userName: null,
  userRole: null,
  token: null,

  setAuth: (data) => set(data),
  clearAuth: () =>
    set({
      authenticated: false,
      userId: null,
      userName: null,
      userRole: null,
      token: null,
    }),
}));
