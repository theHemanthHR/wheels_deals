export default function GoogleMap({ location, height = 260 }) {
  const key = import.meta.env.VITE_GOOGLE_MAPS_KEY;
  if (!key || !location) {
    return (
      <div className="d-flex align-items-center justify-content-center text-muted"
           style={{ height, background: 'var(--surface-2)', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)' }}>
        <div className="text-center">
          <i className="bi bi-geo-alt" style={{ fontSize: '1.6rem' }} />
          <div className="mt-2">{location || 'Location not provided'}</div>
        </div>
      </div>
    );
  }
  const src = `https://www.google.com/maps/embed/v1/place?key=${key}&q=${encodeURIComponent(location)}&zoom=12`;
  return (
    <iframe
      title="Location map" width="100%" height={height}
      style={{ border: 0, borderRadius: 'var(--radius-lg)' }}
      loading="lazy" referrerPolicy="no-referrer-when-downgrade" src={src} />
  );
}
