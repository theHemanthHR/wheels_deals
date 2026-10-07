import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { api, tokenStore } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(tokenStore.getUser());
  const [ready, setReady] = useState(false);

  useEffect(() => { setUser(tokenStore.getUser()); setReady(true); }, []);

  const login = async (email, password) => {
    const { data } = await api.post('/auth/login', { email, password });
    tokenStore.save(data);
    setUser(data.user);
    return data.user;
  };

  const register = async (payload) => {
    const { data } = await api.post('/auth/register', payload);
    tokenStore.save(data);
    setUser(data.user);
    return data.user;
  };

  const loginWithTokens = (accessToken, refreshToken, decodedUser) => {
    tokenStore.save({ accessToken, refreshToken, user: decodedUser });
    setUser(decodedUser);
  };

  const logout = () => { tokenStore.clear(); setUser(null); };

  const value = useMemo(() => ({
    user, ready,
    isAuthenticated: !!user,
    isAdmin: user?.role === 'ADMIN',
    login, register, logout, loginWithTokens,
  }), [user, ready]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}
