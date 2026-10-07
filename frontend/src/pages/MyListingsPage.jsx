import { Link } from 'react-router-dom';
import { useState } from 'react';
import { useMyVehicles, useDeleteVehicle, useMarkSold, useReactivateVehicle } from '../hooks/useVehicles';
import Loader from '../components/Loader';
import Empty from '../components/Empty';
import Pagination from '../components/Pagination';
import { inr } from '../styles/theme';

export default function MyListingsPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = useMyVehicles(page);
  const del = useDeleteVehicle();
  const sold = useMarkSold();
  const react = useReactivateVehicle();

  const doDelete = (id) => { if (window.confirm('Remove this listing?')) del.mutate(id); };
  const doSold = (id) => { if (window.confirm('Mark this listing as SOLD? Other pending offers will be rejected.')) sold.mutate({ id, note: null }); };

  if (isLoading) return <div className="page container"><Loader /></div>;

  return (
    <div className="page">
      <div className="container">
        <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
          <h1 className="section-title mb-0">My listings</h1>
          <Link to="/sell" className="btn btn-accent"><i className="bi bi-plus-lg me-1" /> New listing</Link>
        </div>
        {!data?.content?.length ? (
          <Empty icon="bi-car-front" title="No listings yet" hint="Post your first vehicle to get started."
                 action={<Link to="/sell" className="btn btn-accent">Sell a vehicle</Link>} />
        ) : (
          <>
            <div className="row g-3">
              {data.content.map((v) => (
                <div key={v.id} className="col-12">
                  <div className="card-yc p-3 d-flex flex-column flex-md-row gap-3 align-items-md-center">
                    <img src={v.coverImageUrl} alt="" style={{ width: 140, height: 105, objectFit: 'cover', borderRadius: 8 }} />
                    <div className="flex-grow-1">
                      <div className="d-flex justify-content-between align-items-start gap-2 flex-wrap">
                        <div>
                          <Link to={`/vehicles/${v.id}`} className="fw-semibold text-reset">{v.company} {v.model}</Link>
                          <div className="muted small">{v.year} · {v.mileage?.toLocaleString('en-IN')} km · {v.location}</div>
                          <div className="price mt-1">{inr(v.price)}</div>
                        </div>
                        <span className={`badge-status ${v.status.toLowerCase()}`}>{v.status}</span>
                      </div>
                    </div>
                    <div className="d-flex gap-2 flex-wrap">
                      <Link to={`/vehicles/${v.id}`} className="btn btn-ghost btn-sm">View</Link>
                      <Link to={`/vehicles/${v.id}/edit`} className="btn btn-ghost btn-sm">Edit</Link>
                      {v.status === 'ACTIVE' && <button className="btn btn-ghost btn-sm" onClick={() => doSold(v.id)}>Mark sold</button>}
                      {v.status === 'SOLD' && <button className="btn btn-ghost btn-sm" onClick={() => react.mutate(v.id)}>Reactivate</button>}
                      {v.status !== 'REMOVED' && <button className="btn btn-outline-danger btn-sm" onClick={() => doDelete(v.id)}>Remove</button>}
                    </div>
                  </div>
                </div>
              ))}
            </div>
            <Pagination page={data.number} totalPages={data.totalPages} onChange={setPage} />
          </>
        )}
      </div>
    </div>
  );
}
