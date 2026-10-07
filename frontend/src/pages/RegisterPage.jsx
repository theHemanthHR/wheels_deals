import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { extractError, API_BASE } from '../api/client';
import { toast } from 'react-toastify';

export default function RegisterPage() {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();

  const onSubmit = async (values) => {
    try {
      await registerUser({
        name: values.name.trim(), email: values.email.trim().toLowerCase(),
        password: values.password, phone: values.phone || null,
        city: values.city || null, state: values.state || null,
      });
      toast.success('Account created!');
      navigate('/', { replace: true });
    } catch (e) { toast.error(extractError(e)); }
  };

  const googleUrl = `${API_BASE.replace('/api/v1', '')}/oauth2/authorization/google`;

  return (
    <div className="container page">
      <div className="auth-wrap">
        <div className="card-yc auth-card">
          <h2 className="serif">Create your account</h2>
          <p className="muted mb-4">Join Vahan and start buying or selling.</p>
          <form onSubmit={handleSubmit(onSubmit)} noValidate>
            <div className="mb-3">
              <label className="form-label">Full name</label>
              <input className={`form-control ${errors.name ? 'is-invalid' : ''}`}
                     {...register('name', { required: 'Name is required' })} />
              {errors.name && <div className="invalid-feedback">{errors.name.message}</div>}
            </div>
            <div className="mb-3">
              <label className="form-label">Email</label>
              <input type="email" className={`form-control ${errors.email ? 'is-invalid' : ''}`}
                     {...register('email', { required: 'Email is required' })} />
              {errors.email && <div className="invalid-feedback">{errors.email.message}</div>}
            </div>
            <div className="mb-3">
              <label className="form-label">Password</label>
              <input type="password" className={`form-control ${errors.password ? 'is-invalid' : ''}`}
                     {...register('password', { required: 'Password is required',
                       minLength: { value: 8, message: 'At least 8 characters' } })} />
              {errors.password && <div className="invalid-feedback">{errors.password.message}</div>}
            </div>
            <div className="row g-2 mb-3">
              <div className="col-6">
                <label className="form-label">Phone (optional)</label>
                <input className="form-control" {...register('phone')} />
              </div>
              <div className="col-6">
                <label className="form-label">City (optional)</label>
                <input className="form-control" {...register('city')} />
              </div>
            </div>
            <div className="mb-3">
              <label className="form-label">State (optional)</label>
              <input className="form-control" {...register('state')} />
            </div>
            <button className="btn btn-accent w-100" disabled={isSubmitting}>
              {isSubmitting ? 'Creating…' : 'Create account'}
            </button>
          </form>
          <div className="text-center my-3 muted small">or</div>
          <a href={googleUrl} className="btn btn-outline-dark w-100 d-flex align-items-center justify-content-center gap-2">
            <i className="bi bi-google" /> Sign up with Google
          </a>
          <p className="text-center mt-4 mb-0 small">
            Already have an account? <Link to="/login">Login</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
