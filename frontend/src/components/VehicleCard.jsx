import { Link } from 'react-router-dom';
import { inr } from '../styles/theme';

export default function VehicleCard({ v }) {
  return (
    <Link to={`/vehicles/${v.id}`} className="text-decoration-none text-reset">
      <div className="card-yc vehicle-card h-100">
        {v.coverImageUrl ? (
          <img src={v.coverImageUrl} alt={`${v.company} ${v.model}`} className="cover" />
        ) : (
          <div className="cover d-flex align-items-center justify-content-center text-muted">
            <i className="bi bi-image" style={{ fontSize: '2rem' }} />
          </div>
        )}
        <div className="p-3">
          <div className="d-flex justify-content-between align-items-start gap-2">
            <div className="fw-semibold text-truncate">{v.company} {v.model}</div>
            {v.status !== 'ACTIVE' && <span className={`badge-status ${v.status.toLowerCase()}`}>{v.status}</span>}
          </div>
          <div className="muted small mt-1">
            {v.year} · {v.mileage?.toLocaleString('en-IN')} km · {v.location}
          </div>
          <div className="price mt-2">{inr(v.price)}</div>
        </div>
      </div>
    </Link>
  );
}
