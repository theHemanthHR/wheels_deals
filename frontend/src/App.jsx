import { Route, Routes, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import ProtectedRoute from './components/ProtectedRoute';

import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import OAuthRedirectPage from './pages/OAuthRedirectPage';
import VehicleDetailPage from './pages/VehicleDetailPage';
import SellPage from './pages/SellPage';
import EditVehiclePage from './pages/EditVehiclePage';
import MyListingsPage from './pages/MyListingsPage';
import WishlistPage from './pages/WishlistPage';
import OffersPage from './pages/OffersPage';
import ProfilePage from './pages/ProfilePage';
import AdminPage from './pages/AdminPage';
import NotFoundPage from './pages/NotFoundPage';

export default function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/"                  element={<HomePage />} />
        <Route path="/login"             element={<LoginPage />} />
        <Route path="/register"          element={<RegisterPage />} />
        <Route path="/forgot-password"   element={<ForgotPasswordPage />} />
        <Route path="/oauth2/redirect"   element={<OAuthRedirectPage />} />
        <Route path="/vehicles/:id"      element={<VehicleDetailPage />} />

        <Route path="/sell"              element={<ProtectedRoute><SellPage /></ProtectedRoute>} />
        <Route path="/vehicles/:id/edit" element={<ProtectedRoute><EditVehiclePage /></ProtectedRoute>} />
        <Route path="/my-listings"       element={<ProtectedRoute><MyListingsPage /></ProtectedRoute>} />
        <Route path="/wishlist"          element={<ProtectedRoute><WishlistPage /></ProtectedRoute>} />
        <Route path="/offers"            element={<ProtectedRoute><OffersPage /></ProtectedRoute>} />
        <Route path="/profile"           element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />

        <Route path="/admin"             element={<ProtectedRoute requireAdmin><AdminPage /></ProtectedRoute>} />

        <Route path="/404"               element={<NotFoundPage />} />
        <Route path="*"                  element={<Navigate to="/404" replace />} />
      </Routes>
      <Footer />
    </>
  );
}
