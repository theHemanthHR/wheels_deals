import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useState } from 'react';

export default function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = () => { logout(); navigate('/'); };

  return (
    <nav className="navbar-yc sticky-top">
      <div className="container d-flex align-items-center justify-content-between">
        <Link to="/" className="brand d-flex align-items-center gap-1">
          Vahan<span className="brand-dot">.</span>
        </Link>

        <button className="btn btn-ghost d-md-none" onClick={() => setOpen(!open)} aria-label="Toggle menu">
          <i className="bi bi-list" />
        </button>

        <div className={`d-md-flex align-items-center gap-3 ${open ? 'd-block mt-3' : 'd-none'}`}>
          <NavLink to="/" className="nav-link p-2">Browse</NavLink>
          {isAuthenticated && <NavLink to="/sell" className="nav-link p-2">Sell</NavLink>}
          {isAuthenticated && <NavLink to="/my-listings" className="nav-link p-2">My Listings</NavLink>}
          {isAuthenticated && <NavLink to="/offers" className="nav-link p-2">Offers</NavLink>}
          {isAuthenticated && <NavLink to="/wishlist" className="nav-link p-2">Wishlist</NavLink>}
          {isAdmin && <NavLink to="/admin" className="nav-link p-2">Admin</NavLink>}

          {!isAuthenticated ? (
            <div className="d-flex gap-2">
              <Link to="/login" className="btn btn-ghost">Login</Link>
              <Link to="/register" className="btn btn-accent">Sign up</Link>
            </div>
          ) : (
            <div className="dropdown">
              <button className="btn btn-ghost dropdown-toggle d-flex align-items-center gap-2" data-bs-toggle="dropdown">
                <i className="bi bi-person-circle" />
                {user?.name?.split(' ')[0] || 'Account'}
              </button>
              <ul className="dropdown-menu dropdown-menu-end">
                <li><Link className="dropdown-item" to="/profile">Profile</Link></li>
                <li><Link className="dropdown-item" to="/my-listings">My Listings</Link></li>
                <li><Link className="dropdown-item" to="/offers">Offers</Link></li>
                <li><hr className="dropdown-divider" /></li>
                <li><button className="dropdown-item text-danger" onClick={handleLogout}>Logout</button></li>
              </ul>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}
