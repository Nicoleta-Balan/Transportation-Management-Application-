import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ErrorBoundary } from './components/ErrorBoundary';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/auth/ProtectedRoute';

// Auth Pages
import LoginPage from './components/auth/LoginPage';
import RegisterPage from './components/auth/RegisterPage';
import ForgotPasswordPage from './components/auth/ForgotPasswordPage';

// User Pages
import SearchPage from './components/user/SearchPage';
import BookingPage from './components/booking/BookingPage';
import DashboardPage from './components/user/DashboardPage';

// Admin Pages
import AdminLayout from './components/admin/AdminLayout';
import StationsPage from './components/stations/StationsPage';
import RoutesPage from './components/routes/RoutesPage';

import './App.css';

function App() {
  return (
    <ErrorBoundary sectionName="Application">
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<SearchPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />

            {/* Protected User Routes */}
            <Route
              path="/booking"
              element={
                <ProtectedRoute>
                  <BookingPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <DashboardPage />
                </ProtectedRoute>
              }
            />

            {/* Admin Routes */}
            <Route
              path="/admin"
              element={
                <ProtectedRoute requiredRole="ADMIN">
                  <AdminLayout />
                </ProtectedRoute>
              }
            >
              <Route index element={<Navigate to="/admin/stations" replace />} />
              <Route path="stations" element={<StationsPage />} />
              <Route path="routes" element={<RoutesPage />} />
            </Route>

            {/* Catch all */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ErrorBoundary>
  );
}

export default App;
