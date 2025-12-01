import { Link, Outlet, useLocation } from 'react-router-dom';
import './AdminLayout.css';

export default function AdminLayout() {
    const location = useLocation();

    const isActive = (path: string) => location.pathname === path;

    return (
        <div className="admin-layout">
            <aside className="admin-sidebar">
                <h2 className="admin-sidebar-title">Admin</h2>
                <nav className="admin-nav">
                    <Link
                        to="/admin/stations"
                        className={`admin-nav-item ${isActive('/admin/stations') ? 'active' : ''}`}
                    >
                        Stations Management
                    </Link>
                    <Link
                        to="/admin/routes"
                        className={`admin-nav-item ${isActive('/admin/routes') ? 'active' : ''}`}
                    >
                        Route Management
                    </Link>
                    <Link
                        to="/admin/book-tickets"
                        className={`admin-nav-item ${isActive('/admin/book-tickets') ? 'active' : ''}`}
                    >
                        Book Tickets
                    </Link>
                </nav>
            </aside>
            <main className="admin-content">
                <Outlet />
            </main>
        </div>
    );
}

