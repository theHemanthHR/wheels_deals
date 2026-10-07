import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { api, extractError } from '../api/client';
import { toast } from 'react-toastify';

export default function ForgotPasswordPage() {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();
  const navigate = useNavigate();

  const onSubmit = async (values) => {
    try {
      await api.post('/auth/reset-password', {
        email: values.email.trim().toLowerCase(), newPassword: values.newPassword,
      });
      toast.success('Password updated. You can log in now.');
      navigate('/login');
    } catch (e) { toast.error(extractError(e)); }
  };

  return (
    <div className="container page">
      <div className="auth-wrap">
        <div className="card-yc auth-card">
          <h2 className="serif">Reset password</h2>
          <p className="muted mb-4">Enter your account email and choose a new password.</p>
          <form onSubmit={handleSubmit(onSubmit)} noValidate>
            <div className="mb-3">
              <label className="form-label">Email</label>
              <input type="email" className={`form-control ${errors.email ? 'is-invalid' : ''}`}
                     {...register('email', { required: 'Email is required' })} />
              {errors.email && <div className="invalid-feedback">{errors.email.message}</div>}
            </div>
            <div className="mb-3">
              <label className="form-label">New password</label>
              <input type="password" className={`form-control ${errors.newPassword ? 'is-invalid' : ''}`}
                     {...register('newPassword', { required: 'New password is required',
                       minLength: { value: 8, message: 'At least 8 characters' } })} />
              {errors.newPassword && <div className="invalid-feedback">{errors.newPassword.message}</div>}
            </div>
            <button className="btn btn-accent w-100" disabled={isSubmitting}>
              {isSubmitting ? 'Updating…' : 'Update password'}
            </button>
          </form>
          <p className="text-center mt-4 mb-0 small">
            Remember it? <Link to="/login">Back to login</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
