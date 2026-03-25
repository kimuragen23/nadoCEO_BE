import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { LogIn, Eye, EyeOff, AlertCircle } from 'lucide-react';

const KEYCLOAK_URL = 'http://localhost:30808';
const REALM = 'nadoceo';
const CLIENT_ID = 'nadoceo-frontend';

export function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !password.trim()) {
      setError('아이디와 비밀번호를 입력해주세요.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const tokenUrl = `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`;
      const res = await fetch(tokenUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
          grant_type: 'password',
          client_id: CLIENT_ID,
          username,
          password,
        }),
      });

      if (!res.ok) {
        const data = await res.json().catch(() => null);
        if (data?.error === 'invalid_grant') {
          setError('아이디 또는 비밀번호가 올바르지 않습니다.');
        } else {
          setError('로그인에 실패했습니다. 다시 시도해주세요.');
        }
        return;
      }

      const data = await res.json();
      const token = data.access_token;
      const refreshToken = data.refresh_token;

      // JWT 디코드 (페이로드)
      const payload = JSON.parse(atob(token.split('.')[1]));
      const roles: string[] = payload.realm_access?.roles || [];
      const role = roles.includes('admin')
        ? 'admin'
        : roles.includes('instructor')
          ? 'instructor'
          : 'student';

      setAuth({
        authenticated: true,
        userId: payload.sub || null,
        userName: payload.preferred_username || payload.name || username,
        userRole: role,
        token,
        refreshToken,
      });

      navigate('/course/java-basic/chat');
    } catch (err) {
      console.error('Login error:', err);
      setError('서버에 연결할 수 없습니다. Keycloak이 실행 중인지 확인해주세요.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F4F5F0] flex items-center justify-center font-sans">
      <div className="w-full max-w-md px-6">
        {/* Logo */}
        <div className="text-center mb-10">
          <div className="inline-flex items-center gap-3 mb-4">
            <span className="bg-blue-600 text-white px-2.5 py-1.5 rounded-lg text-sm font-bold">AI</span>
            <h1 className="text-3xl font-bold tracking-tight text-slate-800">NADOCEO</h1>
          </div>
          <p className="text-slate-500 text-sm">IT 교육 코칭 AI — 소크라테스식 학습 도우미</p>
        </div>

        {/* Login Card */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200/60 p-8">
          <h2 className="text-lg font-bold text-slate-800 mb-6">로그인</h2>

          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-600 mb-1.5">아이디</label>
              <Input
                type="text"
                placeholder="아이디를 입력하세요"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="h-11 rounded-xl border-slate-200 focus:border-blue-500 focus:ring-blue-500"
                autoFocus
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-600 mb-1.5">비밀번호</label>
              <div className="relative">
                <Input
                  type={showPassword ? 'text' : 'password'}
                  placeholder="비밀번호를 입력하세요"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="h-11 rounded-xl border-slate-200 focus:border-blue-500 focus:ring-blue-500 pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {error && (
              <div className="flex items-center gap-2 text-sm text-red-600 bg-red-50 rounded-xl px-4 py-3">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <Button
              type="submit"
              disabled={loading}
              className="w-full h-11 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold shadow-sm mt-2"
            >
              {loading ? (
                <span className="flex items-center gap-2">
                  <svg className="animate-spin w-4 h-4" viewBox="0 0 24 24" fill="none">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                  </svg>
                  로그인 중...
                </span>
              ) : (
                <span className="flex items-center gap-2">
                  <LogIn className="w-4 h-4" />
                  로그인
                </span>
              )}
            </Button>
          </form>
        </div>

        {/* Test Accounts */}
        <div className="mt-6 bg-white/50 rounded-xl border border-slate-200/40 p-4">
          <p className="text-xs font-semibold text-slate-400 mb-2">테스트 계정</p>
          <div className="space-y-1 text-xs text-slate-500">
            <p><span className="font-medium text-slate-600">학생:</span> student01 / student01</p>
            <p><span className="font-medium text-slate-600">강사:</span> instructor01 / instructor01</p>
            <p><span className="font-medium text-slate-600">관리자:</span> admin / admin</p>
          </div>
        </div>
      </div>
    </div>
  );
}
