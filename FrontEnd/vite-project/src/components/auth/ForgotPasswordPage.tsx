import { Link, useNavigate } from 'react-router-dom';
import './AuthPages.css';

export default function ForgotPasswordPage() {
    const navigate = useNavigate();

    return (
        <div className="auth-page">
            <div className="auth-container">
                <div className="auth-card">
                    <button className="back-to-home" onClick={() => navigate('/')}>
                        Back to Home
                    </button>

                    <div className="auth-header">
                        <h1>Reset Password</h1>
                        <p>This feature is coming soon</p>
                    </div>

                    <div className="placeholder-content">
                        <div className="placeholder-icon">
                            <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                            </svg>
                        </div>
                        <p className="placeholder-text">
                            Password reset functionality will be available in a future update.
                            <br />
                            <br />
                            For now, please contact support if you need help accessing your account.
                        </p>
                        <Link to="/login" className="auth-submit-btn" style={{ display: 'inline-block', textDecoration: 'none', textAlign: 'center' }}>
                            Back to Login
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
}
