import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ErrorBoundary } from './components/ErrorBoundary';
import AdminLayout from './components/admin/AdminLayout';
import StationsPage from './components/stations/StationsPage';
import RoutesPage from './components/routes/RoutesPage';
import './App.css';

function App() {
  return (
    <ErrorBoundary sectionName="Application">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Navigate to="/admin/stations" replace />} />
          <Route path="/admin" element={<AdminLayout />}>
            <Route path="stations" element={<StationsPage />} />
            <Route path="routes" element={<RoutesPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ErrorBoundary>
  );
}

export default App;
