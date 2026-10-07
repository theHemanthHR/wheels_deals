import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useReceivedOffers, useSentOffers, useOfferDecision } from '../hooks/useInteractions';
import Loader from '../components/Loader';
import Empty from '../components/Empty';
import Pagination from '../components/Pagination';
import { inr, formatDate } from '../styles/theme';

export default function OffersPage() {
  const [tab, setTab] = useState('received');
  return (
    <div className="page">
      <div className="container">
        <h1 className="section-title mb-4">Offers</h1>
        <ul className="nav nav-tabs mb-3">
          <li className="nav-item">
            <button className={`nav-link ${tab === 'received' ? 'active' : ''}`} onClick={() => setTab('received')}>Received</button>
          </li>
          <li className="nav-item">
            <button className={`nav-link ${tab === 'sent' ? 'active' : ''}`} onClick={() => setTab('sent')}>Sent</button>
          </li>
        </ul>
        {tab === 'received' ? <ReceivedOffers /> : <SentOffers />}
      </div>
    </div>
  );
}

function ReceivedOffers() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = useReceivedOffers(page);
  const decision = useOfferDecision();
  if (isLoading) return <Loader />;
  if (!data?.content?.length) return <Empty title="No offers received yet" />;
  return (
    <>
      {data.content.map((o) => (
        <div key={o.id} className="card-yc p-3 mb-2">
          <div className="d-flex justify-content-between flex-wrap gap-2">
            <div>
              <Link to={`/vehicles/${o.vehicleId}`} className="fw-semibold text-reset">{o.vehicleTitle}</Link>
              <div className="muted small">From <strong>{o.buyerName}</strong> · {formatDate(o.createdAt)}</div>
              {o.message && <div className="small mt-1">"{o.message}"</div>}
              <div className="mt-2">
                Asking: <span className="muted">{inr(o.vehiclePrice)}</span> · Offered: <strong>{inr(o.amount)}</strong>
              </div>
            </div>
            <div className="text-end">
              <span className={`badge-status ${o.status.toLowerCase()}`}>{o.status}</span>
              {o.status === 'PENDING' && (
                <div className="d-flex gap-2 mt-2">
                  <button className="btn btn-accent btn-sm" onClick={() => decision.mutate({ id: o.id, action: 'accept' })}>Accept</button>
                  <button className="btn btn-ghost btn-sm" onClick={() => decision.mutate({ id: o.id, action: 'reject' })}>Reject</button>
                </div>
              )}
            </div>
          </div>
        </div>
      ))}
      <Pagination page={data.number} totalPages={data.totalPages} onChange={setPage} />
    </>
  );
}

function SentOffers() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = useSentOffers(page);
  const decision = useOfferDecision();
  if (isLoading) return <Loader />;
  if (!data?.content?.length) return <Empty title="You haven't made any offers yet" />;
  return (
    <>
      {data.content.map((o) => (
        <div key={o.id} className="card-yc p-3 mb-2">
          <div className="d-flex justify-content-between flex-wrap gap-2">
            <div>
              <Link to={`/vehicles/${o.vehicleId}`} className="fw-semibold text-reset">{o.vehicleTitle}</Link>
              <div className="muted small">To <strong>{o.sellerName}</strong> · {formatDate(o.createdAt)}</div>
              {o.message && <div className="small mt-1">"{o.message}"</div>}
              <div className="mt-2">
                Asking: <span className="muted">{inr(o.vehiclePrice)}</span> · Offered: <strong>{inr(o.amount)}</strong>
              </div>
            </div>
            <div className="text-end">
              <span className={`badge-status ${o.status.toLowerCase()}`}>{o.status}</span>
              {o.status === 'PENDING' && (
                <div className="mt-2">
                  <button className="btn btn-ghost btn-sm" onClick={() => decision.mutate({ id: o.id, action: 'withdraw' })}>Withdraw</button>
                </div>
              )}
            </div>
          </div>
        </div>
      ))}
      <Pagination page={data.number} totalPages={data.totalPages} onChange={setPage} />
    </>
  );
}
