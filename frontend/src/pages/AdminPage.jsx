import { useState } from 'react';
import { useAdminUsers, useSetUserEnabled, useAdminVehicles, useAdminRemoveVehicle, useAdminReports, useResolveReport, useAuditLogs } from '../hooks/useAdmin';
import Loader from '../components/Loader';
import Empty from '../components/Empty';
import { formatDate } from '../styles/theme';

export default function AdminPage() {
  const [tab, setTab] = useState('users');
  return (
    <div className="page">
      <div className="container">
        <h1 className="section-title mb-4">Admin dashboard</h1>
        <ul className="nav nav-tabs mb-3">
          {[['users','Users'],['vehicles','Vehicles'],['reports','Reports'],['audit','Audit logs']].map(([k, l]) => (
            <li className="nav-item" key={k}>
              <button className={`nav-link ${tab === k ? 'active' : ''}`} onClick={() => setTab(k)}>{l}</button>
            </li>
          ))}
        </ul>
        {tab === 'users' && <UsersTab />}
        {tab === 'vehicles' && <VehiclesTab />}
        {tab === 'reports' && <ReportsTab />}
        {tab === 'audit' && <AuditTab />}
      </div>
    </div>
  );
}

function UsersTab() {
  const [q, setQ] = useState('');
  const { data, isLoading } = useAdminUsers(q);
  const toggle = useSetUserEnabled();
  if (isLoading) return <Loader />;
  return (
    <>
      <input className="form-control mb-3" placeholder="Search by name or email"
             value={q} onChange={(e) => setQ(e.target.value)} />
      <div className="table-responsive">
        <table className="table table-yc">
          <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Joined</th><th /></tr></thead>
          <tbody>
            {data.content.map((u) => (
              <tr key={u.id}>
                <td>{u.name}</td><td>{u.email}</td>
                <td><span className="badge bg-dark">{u.role}</span></td>
                <td>{u.enabled ? <span className="text-success">Enabled</span> : <span className="text-danger">Disabled</span>}</td>
                <td>{formatDate(u.createdAt)}</td>
                <td>
                  {u.role !== 'ADMIN' && (
                    <button className={`btn btn-sm ${u.enabled ? 'btn-outline-danger' : 'btn-accent'}`}
                            onClick={() => toggle.mutate({ id: u.id, enabled: !u.enabled })}>
                      {u.enabled ? 'Disable' : 'Enable'}
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}

function VehiclesTab() {
  const { data, isLoading } = useAdminVehicles();
  const remove = useAdminRemoveVehicle();
  const doRemove = (id) => {
    const reason = window.prompt('Reason for removal (optional):', 'Violates listing policy');
    if (reason === null) return;
    remove.mutate({ id, reason });
  };
  if (isLoading) return <Loader />;
  if (!data?.content?.length) return <Empty title="No vehicles" />;
  return (
    <div className="table-responsive">
      <table className="table table-yc">
        <thead><tr><th>Vehicle</th><th>Seller</th><th>Status</th><th>Posted</th><th /></tr></thead>
        <tbody>
          {data.content.map((v) => (
            <tr key={v.id}>
              <td>{v.company} {v.model} ({v.year})</td>
              <td>{v.sellerName}<div className="small muted">{v.sellerEmail}</div></td>
              <td><span className={`badge-status ${v.status.toLowerCase()}`}>{v.status}</span></td>
              <td>{formatDate(v.createdAt)}</td>
              <td>{v.status !== 'REMOVED' && <button className="btn btn-outline-danger btn-sm" onClick={() => doRemove(v.id)}>Remove</button>}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function ReportsTab() {
  const [status, setStatus] = useState('');
  const { data, isLoading } = useAdminReports(status);
  const resolve = useResolveReport();
  const act = (r, newStatus) => {
    const note = window.prompt('Admin note (optional):', '') ?? '';
    resolve.mutate({ id: r.id, status: newStatus, adminNote: note });
  };
  if (isLoading) return <Loader />;
  if (!data?.content?.length) return <Empty title="No reports" />;
  return (
    <>
      <div className="mb-3">
        <select className="form-select w-auto" value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All statuses</option>
          <option value="OPEN">Open</option>
          <option value="REVIEWED">Reviewed</option>
          <option value="DISMISSED">Dismissed</option>
        </select>
      </div>
      <div className="table-responsive">
        <table className="table table-yc">
          <thead><tr><th>Vehicle</th><th>Reporter</th><th>Reason</th><th>Status</th><th /></tr></thead>
          <tbody>
            {data.content.map((r) => (
              <tr key={r.id}>
                <td>{r.vehicleTitle}</td>
                <td>{r.reporterName}</td>
                <td className="small">{r.reason}</td>
                <td><span className="badge bg-secondary">{r.status}</span></td>
                <td>
                  {r.status === 'OPEN' && (
                    <div className="d-flex gap-2">
                      <button className="btn btn-sm btn-accent" onClick={() => act(r, 'REVIEWED')}>Mark reviewed</button>
                      <button className="btn btn-sm btn-ghost" onClick={() => act(r, 'DISMISSED')}>Dismiss</button>
                    </div>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}

function AuditTab() {
  const { data, isLoading } = useAuditLogs();
  if (isLoading) return <Loader />;
  if (!data?.content?.length) return <Empty title="No audit logs" />;
  return (
    <div className="table-responsive">
      <table className="table table-yc">
        <thead><tr><th>When</th><th>Admin</th><th>Action</th><th>Target</th><th>Details</th></tr></thead>
        <tbody>
          {data.content.map((a) => (
            <tr key={a.id}>
              <td className="small">{formatDate(a.createdAt)}</td>
              <td className="small">{a.adminEmail}</td>
              <td><code>{a.action}</code></td>
              <td className="small">{a.targetType} #{a.targetId}</td>
              <td className="small">{a.details || '—'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
