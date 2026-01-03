import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ErrorBoundary } from './components/ErrorBoundary';
import AdminLayout from './components/admin/AdminLayout';
import SearchPage from './components/user/SearchPage';
import BookingPage from './components/booking/BookingPage';
import StationsPage from './components/stations/StationsPage';
import RoutesPage from './components/routes/RoutesPage';
import './App.css';

function App() {
  return (
    <ErrorBoundary sectionName="Application">
      <BrowserRouter>
        <Routes>
          {/* User Routes */}
          <Route path="/" element={<SearchPage />} />
          <Route path="/booking" element={<BookingPage />} />

          {/* Admin Routes */}
          <Route path="/admin" element={<AdminLayout />}>
            {/* Redirecționare automată de la /admin la /admin/stations */}
            <Route index element={<Navigate to="/admin/stations" replace />} />

            <Route path="stations" element={<StationsPage />} />
            <Route path="routes" element={<RoutesPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ErrorBoundary>
  );
}

export default App;
