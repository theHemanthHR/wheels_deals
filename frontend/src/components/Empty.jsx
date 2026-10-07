export default function Empty({ icon = 'bi-inbox', title, hint, action }) {
  return (
    <div className="empty">
      <i className={`bi ${icon}`} style={{ fontSize: '2.5rem' }} />
      <h4 className="mt-3 mb-1 serif">{title}</h4>
      {hint && <p className="mb-3">{hint}</p>}
      {action}
    </div>
  );
}
