import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="page container">
      <div className="empty">
        <h1 className="serif">404</h1>
        <p className="muted">This page doesn't exist.</p>
        <Link to="/" className="btn btn-accent">Back home</Link>
      </div>
    </div>
  );
}
