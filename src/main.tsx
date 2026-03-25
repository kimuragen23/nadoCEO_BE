import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.tsx';
import './index.css';
import keycloak from './auth/keycloak';
import { useAuthStore } from './store/authStore';

keycloak
  .init({ onLoad: 'login-required', checkLoginIframe: false, pkceMethod: 'S256' })
  .then((authenticated) => {
    if (authenticated && keycloak.tokenParsed) {
      const parsed = keycloak.tokenParsed as any;
      const roles: string[] = parsed.realm_access?.roles || [];
      const role = roles.includes('admin')
        ? 'admin'
        : roles.includes('instructor')
          ? 'instructor'
          : 'student';

      useAuthStore.getState().setAuth({
        authenticated: true,
        userId: parsed.sub || null,
        userName: parsed.preferred_username || parsed.name || null,
        userRole: role,
        token: keycloak.token || null,
      });

      // 토큰 자동 갱신
      setInterval(() => {
        keycloak.updateToken(30).catch(() => keycloak.logout());
      }, 60000);
    }

    createRoot(document.getElementById('root')!).render(
      <StrictMode>
        <App />
      </StrictMode>,
    );
  })
  .catch((err) => {
    console.error('Keycloak init error:', err);
    // Keycloak 접속 실패 시에도 앱 렌더링 (개발용)
    createRoot(document.getElementById('root')!).render(
      <StrictMode>
        <App />
      </StrictMode>,
    );
  });
