import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { bookingApi } from '../../services/bookingApi';
import type { BookingResponse } from '../../services/bookingApi';
import { authApi } from '../../services/authApi';
import type { UpdateProfileRequest } from '../../services/authApi';
import './DashboardPage.css';

type TabType = 'bookings' | 'history' | 'profile';

// Generate a unique ticket code for QR
const generateTicketCode = (booking: BookingResponse): string => {
    // Create a unique code based on booking details
    const data = `TMS-${booking.id}-${booking.routeId}-${new Date(booking.departureTime).getTime()}`;
    // Simple hash for display
    let hash = 0;
    for (let i = 0; i < data.length; i++) {
        const char = data.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash;
    }
    return `TMS${Math.abs(hash).toString(36).toUpperCase().padStart(8, '0')}`;
};

// Booking Card Component
const BookingCard = ({ booking, isPast }: { booking: BookingResponse; isPast?: boolean }) => {
    const formatDate = (dateStr: string) => {
        return new Date(dateStr).toLocaleDateString('en-US', {
            weekday: 'short',
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });
    };

    const formatTime = (dateStr: string) => {
        return new Date(dateStr).toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getStatusBadge = (status: string) => {
        const statusClasses: Record<string, string> = {
            PENDING: 'status-pending',
            CONFIRMED: 'status-confirmed',
            CANCELLED: 'status-cancelled'
        };
        return statusClasses[status] || '';
    };

    const ticketCode = generateTicketCode(booking);
    // QR code using free API service
    const qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=120x120&data=${encodeURIComponent(ticketCode)}`;

    return (
        <div className={`booking-card ${isPast ? 'past' : ''}`}>
            <div className="booking-header">
                <div className="booking-route">
                    <span className="origin">{booking.originStation}</span>
                    <span className="arrow">→</span>
                    <span className="destination">{booking.destinationStation}</span>
                </div>
                <span className={`status-badge ${getStatusBadge(booking.status)}`}>
                    {booking.status}
                </span>
            </div>

            <div className="booking-body">
                <div className="booking-details">
                    <div className="detail-row">
                        <span className="detail-label">Date:</span>
                        <span className="detail-value">{formatDate(booking.departureTime)}</span>
                    </div>
                    <div className="detail-row">
                        <span className="detail-label">Time:</span>
                        <span className="detail-value">
                            {formatTime(booking.departureTime)} - {formatTime(booking.arrivalTime)}
                        </span>
                    </div>
                    <div className="detail-row">
                        <span className="detail-label">Passenger:</span>
                        <span className="detail-value">{booking.passengerName}</span>
                    </div>
                    <div className="detail-row">
                        <span className="detail-label">Seats:</span>
                        <span className="detail-value">
                            {booking.selectedSeats || `${booking.seatCount} seat(s)`}
                        </span>
                    </div>
                    {booking.totalPrice != null && booking.totalPrice > 0 && (
                        <div className="detail-row">
                            <span className="detail-label">Price:</span>
                            <span className="detail-value">
                                {booking.totalPrice.toFixed(2)} {booking.currency || 'RON'}
                            </span>
                        </div>
                    )}
                </div>

                {/* QR Code Section - only show for active (non-past) bookings */}
                {!isPast && booking.status !== 'CANCELLED' && (
                    <div className="booking-qr">
                        <img
                            src={qrCodeUrl}
                            alt="Ticket QR Code"
                            className="qr-code-img"
                        />
                        <span className="ticket-code">{ticketCode}</span>
                    </div>
                )}
            </div>

            <div className="booking-footer">
                <span className="booking-id">Booking #{booking.id}</span>
                <span className="booked-on">Booked: {formatDate(booking.createdAt)}</span>
            </div>
        </div>
    );
};

export default function DashboardPage() {
    const navigate = useNavigate();
    const { user, logout, refreshUser } = useAuth();
    const [activeTab, setActiveTab] = useState<TabType>('bookings');
    const [activeBookings, setActiveBookings] = useState<BookingResponse[]>([]);
    const [bookingHistory, setBookingHistory] = useState<BookingResponse[]>([]);
    const [bookingsLoading, setBookingsLoading] = useState(false);
    const [historyLoading, setHistoryLoading] = useState(false);

    // Profile editing state
    const [isEditing, setIsEditing] = useState(false);
    const [profileForm, setProfileForm] = useState<UpdateProfileRequest>({});
    const [profileSaving, setProfileSaving] = useState(false);
    const [profileError, setProfileError] = useState<string | null>(null);
    const [profileSuccess, setProfileSuccess] = useState(false);

    // Initialize profile form when user data is available or editing starts
    useEffect(() => {
        if (user && isEditing) {
            setProfileForm({
                firstName: user.firstName || '',
                lastName: user.lastName || '',
                phone: user.phone || '',
                address: user.address || '',
                dateOfBirth: user.dateOfBirth || '',
            });
        }
    }, [user, isEditing]);

    const handleProfileChange = (field: keyof UpdateProfileRequest, value: string) => {
        setProfileForm(prev => ({ ...prev, [field]: value }));
    };

    const handleSaveProfile = async () => {
        setProfileSaving(true);
        setProfileError(null);
        setProfileSuccess(false);

        try {
            await authApi.updateProfile(profileForm);
            await refreshUser();
            setProfileSuccess(true);
            setIsEditing(false);
            setTimeout(() => setProfileSuccess(false), 3000);
        } catch (error) {
            setProfileError(error instanceof Error ? error.message : 'Failed to update profile');
        } finally {
            setProfileSaving(false);
        }
    };

    const handleCancelEdit = () => {
        setIsEditing(false);
        setProfileError(null);
    };

    // Fetch active bookings when tab is selected
    useEffect(() => {
        if (activeTab === 'bookings') {
            setBookingsLoading(true);
            bookingApi.getActiveBookings()
                .then(bookings => {
                    // Filter to only show future bookings (departure time > now)
                    // OR bookings for today that haven't departed yet
                    const now = new Date();
                    const futureBookings = bookings.filter(booking => {
                        const departureTime = new Date(booking.departureTime);
                        // Keep if departure is in future OR if it's today (even if technically passed by minutes, keep it visible for the day)
                        // Actually, strict future check is safer to avoid confusion with history
                        // But user asked for "active route for the day (if the day its in its not due)"
                        
                        // Let's just show all non-cancelled bookings that the backend returns as "active"
                        // The backend usually filters by departureTime > now.
                        // If we want to show bookings for later today, backend should already return them.
                        
                        return departureTime > now && booking.status !== 'CANCELLED';
                    });
                    setActiveBookings(futureBookings);
                })
                .catch(console.error)
                .finally(() => setBookingsLoading(false));
        }
    }, [activeTab]);

    // Fetch booking history when tab is selected
    useEffect(() => {
        if (activeTab === 'history') {
            setHistoryLoading(true);
            bookingApi.getBookingHistory()
                .then(setBookingHistory)
                .catch(console.error)
                .finally(() => setHistoryLoading(false));
        }
    }, [activeTab]);

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    const tabs: { key: TabType; label: string; icon: React.ReactNode }[] = [
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
        }
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
                            {bookingsLoading ? (
                                <div className="loading-state">Loading your bookings...</div>
                            ) : activeBookings.length === 0 ? (
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
                            ) : (
                                <div className="bookings-list">
                                    {activeBookings.map(booking => (
                                        <BookingCard key={booking.id} booking={booking} />
                                    ))}
                                </div>
                            )}
                        </div>
                    )}

                    {activeTab === 'history' && (
                        <div className="tab-content">
                            <h2>Ticket History</h2>
                            <p className="tab-description">View your past bookings and trips</p>
                            {historyLoading ? (
                                <div className="loading-state">Loading your history...</div>
                            ) : bookingHistory.length === 0 ? (
                                <div className="empty-state">
                                    <div className="empty-icon">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                                            <circle cx="12" cy="12" r="10"/>
                                            <polyline points="12 6 12 12 16 14"/>
                                        </svg>
                                    </div>
                                    <p>Your past bookings will appear here.</p>
                                </div>
                            ) : (
                                <div className="bookings-list">
                                    {bookingHistory.map(booking => (
                                        <BookingCard key={booking.id} booking={booking} isPast />
                                    ))}
                                </div>
                            )}
                        </div>
                    )}

                    {activeTab === 'profile' && (
                        <div className="tab-content">
                            <h2>Profile Settings</h2>
                            <p className="tab-description">Manage your personal information</p>

                            {profileSuccess && (
                                <div className="success-message">Profile updated successfully!</div>
                            )}
                            {profileError && (
                                <div className="error-message">{profileError}</div>
                            )}

                            {isEditing ? (
                                <div className="profile-form">
                                    <div className="form-row">
                                        <label htmlFor="email">Email</label>
                                        <input
                                            id="email"
                                            type="email"
                                            value={user?.email || ''}
                                            disabled
                                            className="input-disabled"
                                        />
                                        <span className="field-hint">Email cannot be changed</span>
                                    </div>
                                    <div className="form-row">
                                        <label htmlFor="firstName">First Name</label>
                                        <input
                                            id="firstName"
                                            type="text"
                                            value={profileForm.firstName || ''}
                                            onChange={(e) => handleProfileChange('firstName', e.target.value)}
                                            placeholder="First Name"
                                        />
                                    </div>
                                    <div className="form-row">
                                        <label htmlFor="lastName">Last Name</label>
                                        <input
                                            id="lastName"
                                            type="text"
                                            value={profileForm.lastName || ''}
                                            onChange={(e) => handleProfileChange('lastName', e.target.value)}
                                            placeholder="Last Name"
                                        />
                                    </div>
                                    <div className="form-row">
                                        <label htmlFor="phone">Phone</label>
                                        <input
                                            id="phone"
                                            type="tel"
                                            value={profileForm.phone || ''}
                                            onChange={(e) => handleProfileChange('phone', e.target.value)}
                                            placeholder="Phone Number"
                                        />
                                    </div>
                                    <div className="form-row">
                                        <label htmlFor="address">Address</label>
                                        <input
                                            id="address"
                                            type="text"
                                            value={profileForm.address || ''}
                                            onChange={(e) => handleProfileChange('address', e.target.value)}
                                            placeholder="Address"
                                        />
                                    </div>
                                    <div className="form-row">
                                        <label htmlFor="dateOfBirth">Date of Birth</label>
                                        <input
                                            id="dateOfBirth"
                                            type="date"
                                            value={profileForm.dateOfBirth || ''}
                                            onChange={(e) => handleProfileChange('dateOfBirth', e.target.value)}
                                        />
                                    </div>
                                    <div className="form-actions">
                                        <button
                                            className="save-btn"
                                            onClick={handleSaveProfile}
                                            disabled={profileSaving}
                                        >
                                            {profileSaving ? 'Saving...' : 'Save Changes'}
                                        </button>
                                        <button
                                            className="cancel-btn"
                                            onClick={handleCancelEdit}
                                            disabled={profileSaving}
                                        >
                                            Cancel
                                        </button>
                                    </div>
                                </div>
                            ) : (
                                <>
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
                                            <label>Phone</label>
                                            <span>{user?.phone || 'Not set'}</span>
                                        </div>
                                        <div className="info-row">
                                            <label>Address</label>
                                            <span>{user?.address || 'Not set'}</span>
                                        </div>
                                        <div className="info-row">
                                            <label>Date of Birth</label>
                                            <span>{user?.dateOfBirth || 'Not set'}</span>
                                        </div>
                                        <div className="info-row">
                                            <label>Account Type</label>
                                            <span className="badge">{user?.userType}</span>
                                        </div>
                                    </div>
                                    <button className="edit-btn" onClick={() => setIsEditing(true)}>
                                        Edit Profile
                                    </button>
                                </>
                            )}
                        </div>
                    )}

                </main>
            </div>
        </div>
    );
}
