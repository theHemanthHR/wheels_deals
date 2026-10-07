import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { tokenStore } from '../api/client';
import { toast } from 'react-toastify';
import Loader from '../components/Loader';

export default function OAuthRedirectPage() {
  const navigate = useNavigate();
  const { loginWithTokens } = useAuth();

  useEffect(() => {
    const hash = window.location.hash.startsWith('#') ? window.location.hash.slice(1) : window.location.hash;
    const params = new URLSearchParams(hash);
    const accessToken = params.get('accessToken');
    const refreshToken = params.get('refreshToken');
    const error = params.get('error');

    if (error) { toast.error(`Sign-in failed: ${error}`); navigate('/login', { replace: true }); return; }
    if (!accessToken || !refreshToken) { toast.error('Missing tokens from Google sign-in'); navigate('/login', { replace: true }); return; }

    const decodeJwt = (t) => {
      try {
        const payload = t.split('.')[1];
        return JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
      } catch { return {}; }
    };

    const decoded = decodeJwt(accessToken);
    const email = decoded.sub || '';
    const role = decoded.role || 'USER';
    const id = decoded.uid;
    const user = { id, email, name: email.split('@')[0], role };

    tokenStore.save({ accessToken, refreshToken, user });
    loginWithTokens(accessToken, refreshToken, user);
    toast.success('Signed in with Google');
    window.history.replaceState(null, '', window.location.pathname);
    navigate('/', { replace: true });
  }, [navigate, loginWithTokens]);

  return <div className="page container"><Loader label="Signing you in…" /></div>;
}
