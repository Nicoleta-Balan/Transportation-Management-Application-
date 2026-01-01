import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ErrorBoundary } from './components/ErrorBoundary';
import AdminLayout from './components/admin/AdminLayout';
import UserLayout from './components/user/UserLayout';
import SearchPage from './components/user/SearchPage';
import StationsPage from './components/stations/StationsPage';
import RoutesPage from './components/routes/RoutesPage';
import './App.css';

function App() {
  return (
    <ErrorBoundary sectionName="Application">
      <BrowserRouter>
        <Routes>
          {/* User Routes */}
          <Route path="/" element={<UserLayout />}>
            <Route index element={<SearchPage />} />
          </Route>

          {/* Admin Routes */}
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
