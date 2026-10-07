export default function Loader({ label = 'Loading…' }) {
  return (
    <div className="d-flex flex-column align-items-center justify-content-center py-5 text-muted">
      <div className="spinner-border text-secondary mb-3" role="status" />
      <div>{label}</div>
    </div>
  );
}
