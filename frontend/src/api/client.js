import axios from 'axios';
import { toast } from 'react-toastify';

export const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
const TOKEN_KEY = 'ecom.accessToken';
const REFRESH_KEY = 'ecom.refreshToken';
const USER_KEY = 'ecom.user';

export const tokenStore = {
  getAccess:  () => localStorage.getItem(TOKEN_KEY),
  getRefresh: () => localStorage.getItem(REFRESH_KEY),
  getUser:    () => { try { return JSON.parse(localStorage.getItem(USER_KEY) || 'null'); } catch { return null; } },
  save: (auth) => {
    localStorage.setItem(TOKEN_KEY, auth.accessToken);
    localStorage.setItem(REFRESH_KEY, auth.refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify(auth.user));
  },
  clear: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    localStorage.removeItem(USER_KEY);
  },
};

export const api = axios.create({ baseURL: API_BASE });

api.interceptors.request.use((config) => {
  const token = tokenStore.getAccess();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

let refreshPromise = null;

api.interceptors.response.use(
  (res) => res,
  async (err) => {
    const original = err.config || {};
    const status = err.response?.status;
    const isAuthEndpoint = original.url?.includes('/auth/login')
                        || original.url?.includes('/auth/register')
                        || original.url?.includes('/auth/refresh')
                        || original.url?.includes('/auth/reset-password');

    if (status === 401 && !original._retry && !isAuthEndpoint) {
      const refreshToken = tokenStore.getRefresh();
      if (!refreshToken) {
        tokenStore.clear();
        if (!window.location.pathname.startsWith('/login')) window.location.href = '/login';
        return Promise.reject(err);
      }
      original._retry = true;
      try {
        refreshPromise = refreshPromise || api.post('/auth/refresh', { refreshToken }).then((r) => r.data).finally(() => { refreshPromise = null; });
        const auth = await refreshPromise;
        tokenStore.save(auth);
        original.headers.Authorization = `Bearer ${auth.accessToken}`;
        return api(original);
      } catch (refreshErr) {
        tokenStore.clear();
        window.location.href = '/login';
        return Promise.reject(refreshErr);
      }
    }

    if (status >= 500) {
    toast.error(err.response?.data?.message || 'Server error. Please try again.', {
      toastId: `server-error-${status}`,  // ✅ React-toastify dedupes identical IDs
    });
    }
    return Promise.reject(err);
  }
);

export const extractError = (err) => {
  const d = err?.response?.data;
  if (!d) return err?.message || 'Something went wrong';
  if (d.errors && typeof d.errors === 'object') return Object.values(d.errors).join(', ');
  return d.message || d.error || 'Something went wrong';
};
