import { useState } from 'react';
import { useWishlist, useWishlistToggle } from '../hooks/useInteractions';
import { Link } from 'react-router-dom';
import Loader from '../components/Loader';
import Empty from '../components/Empty';
import Pagination from '../components/Pagination';
import { inr, formatDate } from '../styles/theme';

export default function WishlistPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = useWishlist(page);
  const toggle = useWishlistToggle();

  if (isLoading) return <div className="page container"><Loader /></div>;

  return (
    <div className="page">
      <div className="container">
        <h1 className="section-title mb-4">Wishlist</h1>
        {!data?.content?.length ? (
          <Empty icon="bi-heart" title="Your wishlist is empty" hint="Save vehicles you're interested in."
                 action={<Link to="/" className="btn btn-accent">Browse vehicles</Link>} />
        ) : (
          <>
            <div className="row g-3">
              {data.content.map((w) => (
                <div className="col-12 col-md-6" key={w.vehicleId}>
                  <div className="card-yc p-3 d-flex gap-3 align-items-center">
                    {w.coverImageUrl && <img src={w.coverImageUrl} alt="" style={{ width: 120, height: 90, objectFit: 'cover', borderRadius: 8 }} />}
                    <div className="flex-grow-1">
                      <Link to={`/vehicles/${w.vehicleId}`} className="fw-semibold text-reset">{w.company} {w.model}</Link>
                      <div className="muted small">{w.year}</div>
                      <div className="price">{inr(w.price)}</div>
                      <div className="muted small">Added {formatDate(w.addedAt)}</div>
                    </div>
                    <button className="btn btn-ghost btn-sm"
                            onClick={() => toggle.mutate({ vehicleId: w.vehicleId, add: false })}>Remove</button>
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
