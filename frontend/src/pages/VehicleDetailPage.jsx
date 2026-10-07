import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useVehicle } from '../hooks/useVehicles';
import {
  useBuyerThreads,
  useSellerThreads,      // ← add this line
  usePostInquiry,
  useCreateOffer,
  useWishlistToggle,
  useCreateReport,
  useCreateReview,
} from '../hooks/useInteractions';
import { useAuth } from '../context/AuthContext';
import Loader from '../components/Loader';
import GoogleMap from '../components/GoogleMap';
import { inr, formatDate } from '../styles/theme';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';

export default function VehicleDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const { data: v, isLoading, error } = useVehicle(id);
  const wishlistToggle = useWishlistToggle();
  const [activeImg, setActiveImg] = useState(0);
  const [showReport, setShowReport] = useState(false);
  const [showReview, setShowReview] = useState(false);

  if (isLoading) return <div className="page container"><Loader /></div>;
  if (error || !v) {
    return (
      <div className="page container">
        <div className="empty">
          <h4 className="serif">Listing not found</h4>
          <p>It may have been removed or the link is broken.</p>
          <Link to="/" className="btn btn-accent">Back to browse</Link>
        </div>
      </div>
    );
  }

  const isOwner = user?.id === v.seller.id;
  const canInteract = isAuthenticated && !isOwner && v.status === 'ACTIVE';

  const onWishlist = () => {
    if (!isAuthenticated) { toast.info('Please log in to save this vehicle.'); navigate('/login'); return; }
    wishlistToggle.mutate({ vehicleId: v.id, add: !v.wishlistedByMe });
  };

  return (
    <div className="page">
      <div className="container">
        <button className="btn btn-ghost mb-3" onClick={() => navigate(-1)}>
          <i className="bi bi-arrow-left" /> Back
        </button>

        <div className="row g-4">
          <div className="col-lg-8">
            <div className="card-yc p-2 mb-3">
              <img src={v.images[activeImg]?.url} alt="vehicle"
                   style={{ width: '100%', maxHeight: 480, objectFit: 'cover', borderRadius: 12 }} />
              <div className="d-flex gap-2 mt-2 overflow-auto">
                {v.images.map((img, i) => (
                  <img key={img.id} src={img.url} alt="thumb" className="image-thumb"
                       style={{ width: 84, height: 84, opacity: i === activeImg ? 1 : 0.6 }}
                       onClick={() => setActiveImg(i)} />
                ))}
              </div>
            </div>

            <div className="card-yc p-4 mb-3">
              <div className="d-flex justify-content-between align-items-start flex-wrap gap-2">
                <div>
                  <h1 className="h3 serif mb-1">{v.company} {v.model}</h1>
                  <div className="muted">{v.year} · {v.mileage?.toLocaleString('en-IN')} km · {v.location}</div>
                </div>
                <div className="text-end">
                  <div className="h3 serif mb-0">{inr(v.price)}</div>
                  <span className={`badge-status ${v.status.toLowerCase()}`}>{v.status}</span>
                </div>
              </div>
              <hr />
              <h5 className="serif">Description</h5>
              <p style={{ whiteSpace: 'pre-wrap' }}>{v.description}</p>
            </div>

            <div className="card-yc p-4 mb-3">
              <h5 className="serif mb-2">Location</h5>
              <GoogleMap location={v.location} />
            </div>

            {canInteract && <InquiryPanel vehicle={v} />}
            {isOwner && <SellerInbox vehicleId={v.id} />}
          </div>

          <div className="col-lg-4">
            <div className="card-yc p-4 mb-3">
              <h5 className="serif mb-3">Seller</h5>
              <div className="d-flex align-items-center gap-3 mb-3">
                {v.seller.avatarUrl
                  ? <img src={v.seller.avatarUrl} alt="" style={{ width: 48, height: 48, borderRadius: '50%', objectFit: 'cover' }} />
                  : <div style={{ width: 48, height: 48, borderRadius: '50%', background: 'var(--surface-2)' }} className="d-flex align-items-center justify-content-center"><i className="bi bi-person" /></div>}
                <div>
                  <div className="fw-semibold">{v.seller.name}</div>
                  <div className="small muted">{v.seller.city || '—'}{v.seller.state ? `, ${v.seller.state}` : ''}</div>
                </div>
              </div>
              <div className="small muted mb-3">
                <i className="bi bi-star-fill text-warning me-1" />
                {v.seller.averageRating ?? '—'} rating · {v.seller.reviewCount} reviews
              </div>

              {isAuthenticated ? (
                v.contact ? (
                  <div className="contact-panel">
                    <div className="small muted mb-1">Contact the seller</div>
                    {v.contact.phone && <div className="mb-1"><i className="bi bi-telephone me-2" />{v.contact.phone}</div>}
                    {v.contact.email && <div><i className="bi bi-envelope me-2" />{v.contact.email}</div>}
                  </div>
                ) : (
                  <div className="contact-panel small muted">This seller has hidden their contact details. Please use the Q&amp;A.</div>
                )
              ) : (
                <div className="contact-panel small"><Link to="/login">Log in</Link> to view the seller's phone &amp; email.</div>
              )}
            </div>

            {canInteract && (
              <div className="card-yc p-4 mb-3">
                <h5 className="serif mb-3">Make an offer</h5>
                <MakeOfferForm vehicleId={v.id} />
              </div>
            )}

            <div className="card-yc p-4">
              <div className="d-grid gap-2">
                <button className="btn btn-outline-dark" onClick={onWishlist}>
                  <i className={`bi ${v.wishlistedByMe ? 'bi-heart-fill' : 'bi-heart'} me-2`} />
                  {v.wishlistedByMe ? 'Remove from wishlist' : 'Add to wishlist'}
                </button>
                {isAuthenticated && !isOwner && (
                  <>
                    <button className="btn btn-ghost" onClick={() => setShowReport((s) => !s)}>
                      <i className="bi bi-flag me-2" /> Report listing
                    </button>
                    <button className="btn btn-ghost" onClick={() => setShowReview((s) => !s)}>
                      <i className="bi bi-star me-2" /> Write a review
                    </button>
                  </>
                )}
                {isOwner && (
                  <Link to={`/vehicles/${v.id}/edit`} className="btn btn-accent">
                    <i className="bi bi-pencil me-2" /> Edit listing
                  </Link>
                )}
              </div>
              {showReport && <ReportForm vehicleId={v.id} onDone={() => setShowReport(false)} />}
              {showReview && <ReviewForm vehicleId={v.id} onDone={() => setShowReview(false)} />}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function InquiryPanel({ vehicle }) {
  const { data: threads = [], isLoading } = useBuyerThreads(vehicle.id);
  const { register, handleSubmit, reset, formState: { isSubmitting } } = useForm();
  const post = usePostInquiry(vehicle.id);
  const { user } = useAuth();

  const submit = async (values) => { await post.mutateAsync({ message: values.message }); reset(); };

  return (
    <div className="card-yc p-4 mb-3">
      <h5 className="serif mb-3">Ask the seller a question</h5>
      <p className="muted small">Only you and the seller can see this conversation.</p>
      <form onSubmit={handleSubmit(submit)} className="d-flex gap-2 mb-4">
        <input className="form-control" placeholder="Is the price negotiable?"
               {...register('message', { required: true })} />
        <button className="btn btn-accent" disabled={isSubmitting || post.isPending}>Send</button>
      </form>
      {isLoading ? <Loader label="Loading conversations…" /> :
        !threads.length ? <div className="muted small">No questions yet.</div> :
        threads.map((t) => (
          <div key={t.rootId} className="thread">
            <div className={`msg ${t.root.buyerId === user.id ? 'buyer' : 'seller'}`}>
              <div className="msg-meta">{t.root.buyerName} · {formatDate(t.root.createdAt)}</div>
              {t.root.message}
            </div>
            {t.replies.map((r) => (
              <div key={r.id} className={`msg ${r.buyerId === user.id ? 'buyer' : 'seller'}`}>
                <div className="msg-meta">{r.buyerId === user.id ? r.buyerName : r.sellerName} · {formatDate(r.createdAt)}</div>
                {r.message}
              </div>
            ))}
            <ReplyBox vehicleId={vehicle.id} parentId={t.rootId} />
          </div>
        ))}
    </div>
  );
}

function ReplyBox({ vehicleId, parentId }) {
  const { register, handleSubmit, reset } = useForm();
  const post = usePostInquiry(vehicleId);
  const submit = async (values) => { await post.mutateAsync({ message: values.message, parentId }); reset(); };
  return (
    <form onSubmit={handleSubmit(submit)} className="d-flex gap-2 mt-2">
      <input className="form-control form-control-sm" placeholder="Reply…" {...register('message', { required: true })} />
      <button className="btn btn-sm btn-ghost">Reply</button>
    </form>
  );
}

function SellerInbox({ vehicleId }) {
  const { data: threads = [], isLoading } = useSellerThreads(vehicleId);
  const { user } = useAuth();
  if (isLoading) return <Loader label="Loading inbox…" />;
  if (!threads.length) {
    return (
      <div className="card-yc p-4 mb-3">
        <h5 className="serif mb-2">Buyer questions</h5>
        <div className="muted small">No questions yet.</div>
      </div>
    );
  }
  return (
    <div className="card-yc p-4 mb-3">
      <h5 className="serif mb-3">Buyer questions</h5>
      {threads.map((t) => (
        <div key={t.rootId} className="thread">
          <div className="msg buyer">
            <div className="msg-meta">{t.root.buyerName} · {formatDate(t.root.createdAt)}</div>
            {t.root.message}
          </div>
          {t.replies.map((r) => (
            <div key={r.id} className={`msg ${r.buyerId === user.id ? 'seller' : 'buyer'}`}>
              <div className="msg-meta">{r.buyerId === user.id ? 'You' : r.buyerName} · {formatDate(r.createdAt)}</div>
              {r.message}
            </div>
          ))}
          <ReplyBox vehicleId={vehicleId} parentId={t.rootId} />
        </div>
      ))}
    </div>
  );
}

function MakeOfferForm({ vehicleId }) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm();
  const offer = useCreateOffer(vehicleId);
  const submit = async (values) => { await offer.mutateAsync({ amount: Number(values.amount), message: values.message }); reset(); };
  return (
    <form onSubmit={handleSubmit(submit)}>
      <div className="mb-2">
        <label className="form-label">Your price (₹)</label>
        <input type="number" step="100" className={`form-control ${errors.amount ? 'is-invalid' : ''}`}
               {...register('amount', { required: 'Amount required', min: { value: 1, message: 'Must be positive' } })} />
        {errors.amount && <div className="invalid-feedback">{errors.amount.message}</div>}
      </div>
      <div className="mb-2">
        <label className="form-label">Message (optional)</label>
        <textarea rows="2" className="form-control" {...register('message')} />
      </div>
      <button className="btn btn-accent w-100" disabled={offer.isPending}>
        {offer.isPending ? 'Sending…' : 'Send offer'}
      </button>
    </form>
  );
}

function ReportForm({ vehicleId, onDone }) {
  const { register, handleSubmit } = useForm();
  const report = useCreateReport(vehicleId);
  const submit = async (values) => { await report.mutateAsync(values.reason); onDone?.(); };
  return (
    <form onSubmit={handleSubmit(submit)} className="mt-3">
      <textarea rows="2" className="form-control mb-2" placeholder="Why are you reporting this listing?"
                {...register('reason', { required: true, maxLength: 500 })} />
      <button className="btn btn-danger w-100">Submit report</button>
    </form>
  );
}

function ReviewForm({ vehicleId, onDone }) {
  const { register, handleSubmit } = useForm({ defaultValues: { rating: 5 } });
  const review = useCreateReview(vehicleId);
  const submit = async (values) => { await review.mutateAsync({ rating: Number(values.rating), comment: values.comment }); onDone?.(); };
  return (
    <form onSubmit={handleSubmit(submit)} className="mt-3">
      <label className="form-label">Rating</label>
      <select className="form-select mb-2" {...register('rating')}>
        {[5, 4, 3, 2, 1].map((n) => <option key={n} value={n}>{n} ★</option>)}
      </select>
      <textarea rows="2" className="form-control mb-2" placeholder="Optional comment" {...register('comment')} />
      <button className="btn btn-accent w-100">Submit review</button>
      <div className="form-text">You can review only after your offer is accepted.</div>
    </form>
  );
}
