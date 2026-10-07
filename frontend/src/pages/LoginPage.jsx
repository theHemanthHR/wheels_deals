import { useForm } from 'react-hook-form';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { extractError, API_BASE } from '../api/client';
import { toast } from 'react-toastify';

export default function LoginPage() {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/';

  const onSubmit = async (values) => {
    try {
      await login(values.email.trim(), values.password);
      toast.success('Welcome back!');
      navigate(from, { replace: true });
    } catch (e) { toast.error(extractError(e)); }
  };

  const googleUrl = `${API_BASE.replace('/api/v1', '')}/oauth2/authorization/google`;

  return (
    <div className="container page">
      <div className="auth-wrap">
        <div className="card-yc auth-card">
          <h2 className="serif">Login</h2>
          <p className="muted mb-4">Welcome back to Vahan.</p>
          <form onSubmit={handleSubmit(onSubmit)} noValidate>
            <div className="mb-3">
              <label className="form-label">Email</label>
              <input type="email" className={`form-control ${errors.email ? 'is-invalid' : ''}`}
                     {...register('email', { required: 'Email is required' })} />
              {errors.email && <div className="invalid-feedback">{errors.email.message}</div>}
            </div>
            <div className="mb-3">
              <div className="d-flex justify-content-between">
                <label className="form-label">Password</label>
                <Link to="/forgot-password" className="small">Forgot password?</Link>
              </div>
              <input type="password" className={`form-control ${errors.password ? 'is-invalid' : ''}`}
                     {...register('password', { required: 'Password is required' })} />
              {errors.password && <div className="invalid-feedback">{errors.password.message}</div>}
            </div>
            <button className="btn btn-accent w-100" disabled={isSubmitting}>
              {isSubmitting ? 'Signing in…' : 'Sign in'}
            </button>
          </form>
          <div className="text-center my-3 muted small">or</div>
          <a href={googleUrl} className="btn btn-outline-dark w-100 d-flex align-items-center justify-content-center gap-2">
            <i className="bi bi-google" /> Continue with Google
          </a>
          <p className="text-center mt-4 mb-0 small">
            Don't have an account? <Link to="/register">Sign up</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
