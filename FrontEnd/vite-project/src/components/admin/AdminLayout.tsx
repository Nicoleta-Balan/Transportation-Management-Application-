import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import './AdminLayout.css';

export default function AdminLayout() {
    const location = useLocation();
    const navigate = useNavigate();

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
                </nav>
                
                {/* Back to Home Button at the bottom of the sidebar */}
                <div style={{ marginTop: 'auto', padding: '1rem' }}>
                    <button 
                        onClick={() => navigate('/')}
                        style={{
                            width: '100%',
                            padding: '0.75rem',
                            backgroundColor: '#1a1a1a',
                            color: 'white',
                            border: '1px solid #333',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontWeight: '600',
                            transition: 'background-color 0.2s'
                        }}
                        onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#333'}
                        onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#1a1a1a'}
                    >
                        Back to Home
                    </button>
                </div>
            </aside>
            <main className="admin-content">
                <Outlet />
            </main>
        </div>
    );
}
