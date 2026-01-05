import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import './DashboardPage.css';

type TabType = 'bookings' | 'history' | 'profile' | 'payment';

export default function DashboardPage() {
    const navigate = useNavigate();
    const { user, logout } = useAuth();
    const [activeTab, setActiveTab] = useState<TabType>('bookings');

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    const tabs: { key: TabType; label: string; icon: React.ReactNode }[] = [
        {
            key: 'bookings',
            label: 'Active Bookings',
            icon: (
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                    <line x1="16" y1="2" x2="16" y2="6"/>
                    <line x1="8" y1="2" x2="8" y2="6"/>
                    <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
            )
        },
        {
            key: 'history',
            label: 'Ticket History',
            icon: (
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <polyline points="12 6 12 12 16 14"/>
                </svg>
            )
        },
        {
            key: 'profile',
            label: 'Profile Settings',
            icon: (
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                </svg>
            )
        },
        {
            key: 'payment',
            label: 'Payment Methods',
            icon: (
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
                    <line x1="1" y1="10" x2="23" y2="10"/>
                </svg>
            )
        },
    ];

    return (
        <div className="dashboard-page">
            <header className="dashboard-header">
                <div className="header-left">
                    <button className="back-home-btn" onClick={() => navigate('/')}>
                        BusBooking
                    </button>
                </div>
                <div className="header-right">
                    <span className="user-greeting">
                        Hello, {user?.firstName}!
                    </span>
                    <button className="logout-btn" onClick={handleLogout}>
                        Sign Out
                    </button>
                </div>
            </header>

            <div className="dashboard-container">
                <aside className="dashboard-sidebar">
                    <div className="sidebar-header">
                        <h2>My Account</h2>
                    </div>
                    <nav className="dashboard-nav">
                        {tabs.map(tab => (
                            <button
                                key={tab.key}
                                className={`nav-item ${activeTab === tab.key ? 'active' : ''}`}
                                onClick={() => setActiveTab(tab.key)}
                            >
                                {tab.icon}
                                <span>{tab.label}</span>
                            </button>
                        ))}
                    </nav>
                </aside>

                <main className="dashboard-content">
                    {activeTab === 'bookings' && (
                        <div className="tab-content">
                            <h2>Active Bookings</h2>
                            <p className="tab-description">View and manage your upcoming trips</p>
                            <div className="empty-state">
                                <div className="empty-icon">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                                        <line x1="16" y1="2" x2="16" y2="6"/>
                                        <line x1="8" y1="2" x2="8" y2="6"/>
                                        <line x1="3" y1="10" x2="21" y2="10"/>
                                    </svg>
                                </div>
                                <p>You don't have any active bookings.</p>
                                <button
                                    className="cta-btn"
                                    onClick={() => navigate('/')}
                                >
                                    Search for Routes
                                </button>
                            </div>
                        </div>
                    )}

                    {activeTab === 'history' && (
                        <div className="tab-content">
                            <h2>Ticket History</h2>
                            <p className="tab-description">View your past bookings and trips</p>
                            <div className="empty-state">
                                <div className="empty-icon">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                                        <circle cx="12" cy="12" r="10"/>
                                        <polyline points="12 6 12 12 16 14"/>
                                    </svg>
                                </div>
                                <p>Your past bookings will appear here.</p>
                            </div>
                        </div>
                    )}

                    {activeTab === 'profile' && (
                        <div className="tab-content">
                            <h2>Profile Settings</h2>
                            <p className="tab-description">Manage your personal information</p>
                            <div className="profile-info">
                                <div className="info-row">
                                    <label>First Name</label>
                                    <span>{user?.firstName}</span>
                                </div>
                                <div className="info-row">
                                    <label>Last Name</label>
                                    <span>{user?.lastName}</span>
                                </div>
                                <div className="info-row">
                                    <label>Email</label>
                                    <span>{user?.email}</span>
                                </div>
                                <div className="info-row">
                                    <label>Account Type</label>
                                    <span className="badge">{user?.userType}</span>
                                </div>
                            </div>
                            <button className="edit-btn" disabled>
                                Edit Profile (Coming Soon)
                            </button>
                        </div>
                    )}

                    {activeTab === 'payment' && (
                        <div className="tab-content">
                            <h2>Payment Methods</h2>
                            <p className="tab-description">Manage your saved payment methods</p>
                            <div className="empty-state">
                                <div className="empty-icon">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                                        <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
                                        <line x1="1" y1="10" x2="23" y2="10"/>
                                    </svg>
                                </div>
                                <p>No saved payment methods.</p>
                                <button className="cta-btn" disabled>
                                    Add Payment Method (Coming Soon)
                                </button>
                            </div>
                        </div>
                    )}
                </main>
            </div>
        </div>
    );
}
