import { Link, Outlet } from 'react-router-dom';
import './UserLayout.css';

export default function UserLayout() {
    return (
        <div className="user-layout">
            <header className="user-header">
                <div className="user-container header-content">
                    <Link to="/" className="logo">
                        TMS Travel
                    </Link>
                    <nav className="user-nav">
                        <Link to="/" className="nav-link">Search</Link>
                        <Link to="/admin" className="nav-link admin-link">Admin Portal</Link>
                    </nav>
                </div>
            </header>
            <main className="user-content">
                <Outlet />
            </main>
            <footer className="user-footer">
                <div className="user-container">
                    <p>&copy; 2024 Transportation Management System</p>
                </div>
            </footer>
        </div>
    );
}
