import { createContext, useContext, useMemo, useState, ReactNode } from 'react';
import { api } from '../api/client';
import type { AuthResponse, UserSummary } from '../api/types';

interface AuthState {
  user: UserSummary | null;
  login: (email: string, password: string) => Promise<UserSummary>;
  logout: () => void;
  hasRole: (...roles: string[]) => boolean;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

function loadUser(): UserSummary | null {
  const raw = localStorage.getItem('feebridge_user');
  return raw ? (JSON.parse(raw) as UserSummary) : null;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserSummary | null>(loadUser());

  const value = useMemo<AuthState>(
    () => ({
      user,
      async login(email, password) {
        const { data } = await api.post<AuthResponse>('/api/auth/login', { email, password });
        localStorage.setItem('feebridge_token', data.token);
        localStorage.setItem('feebridge_user', JSON.stringify(data.user));
        setUser(data.user);
        return data.user;
      },
      logout() {
        localStorage.removeItem('feebridge_token');
        localStorage.removeItem('feebridge_user');
        setUser(null);
      },
      hasRole(...roles) {
        return !!user && roles.some((r) => user.roles.includes(r));
      },
    }),
    [user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
