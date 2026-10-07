import { useAuth } from '../context/AuthContext';
import { useUserReviews } from '../hooks/useInteractions';
import { formatDate } from '../styles/theme';
import Loader from '../components/Loader';
import { useState } from 'react';
import { Link } from 'react-router-dom';

export default function ProfilePage() {
  const { user, logout } = useAuth();
  const [page] = useState(0);
  const { data: reviews, isLoading } = useUserReviews(user?.id, page);
  if (!user) return null;
  return (
    <div className="page">
      <div className="container" style={{ maxWidth: 800 }}>
        <h1 className="section-title mb-4">My profile</h1>
        <div className="card-yc p-4 mb-3">
          <div className="d-flex align-items-center gap-3 mb-3">
            {user.avatarUrl
              ? <img src={user.avatarUrl} alt="" style={{ width: 72, height: 72, borderRadius: '50%' }} />
              : <div style={{ width: 72, height: 72, borderRadius: '50%', background: 'var(--surface-2)' }} className="d-flex align-items-center justify-content-center"><i className="bi bi-person" style={{ fontSize: '2rem' }} /></div>}
            <div>
              <h4 className="serif mb-0">{user.name}</h4>
              <div className="muted">{user.email}</div>
              {user.role === 'ADMIN' && <span className="badge bg-dark mt-2">Admin</span>}
            </div>
          </div>
          <dl className="row mb-0">
            <dt className="col-sm-3 muted">City</dt><dd className="col-sm-9">{user.city || '—'}</dd>
            <dt className="col-sm-3 muted">State</dt><dd className="col-sm-9">{user.state || '—'}</dd>
            <dt className="col-sm-3 muted">Phone</dt><dd className="col-sm-9">{user.phone || '—'}</dd>
          </dl>
        </div>
        <div className="card-yc p-4 mb-3">
          <h5 className="serif mb-3">Reviews about you</h5>
          {isLoading ? <Loader /> : !reviews?.content?.length ? (
            <div className="muted small">No reviews yet.</div>
          ) : (
            reviews.content.map((r) => (
              <div key={r.id} className="border-bottom py-2">
                <div className="d-flex justify-content-between">
                  <strong>{r.reviewerName}</strong>
                  <span>{'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}</span>
                </div>
                <div className="small">{r.comment}</div>
                <div className="muted small">{formatDate(r.createdAt)}</div>
              </div>
            ))
          )}
        </div>
        <div className="card-yc p-4">
          <h5 className="serif mb-3">Danger zone</h5>
          <button className="btn btn-outline-danger" onClick={logout}>Log out</button>
          <Link to="/my-listings" className="btn btn-ghost mt-2">Manage my listings</Link>
        </div>
      </div>
    </div>
  );
}
