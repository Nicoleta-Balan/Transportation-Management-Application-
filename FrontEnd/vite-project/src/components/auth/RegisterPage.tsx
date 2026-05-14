import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import './AuthPages.css';

export default function RegisterPage() {
    const navigate = useNavigate();
    const { register, isAuthenticated, user } = useAuth();

    const [formData, setFormData] = useState({
        email: '',
        password: '',
        confirmPassword: '',
        firstName: '',
        lastName: '',
        phone: '',
        address: '',
        dateOfBirth: '',
    });
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    // Redirect if already authenticated
    useEffect(() => {
        if (isAuthenticated && user) {
            if (user.userType === 'ADMIN') {
                navigate('/admin', { replace: true });
            } else {
                navigate('/dashboard', { replace: true });
            }
        }
    }, [isAuthenticated, user, navigate]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const validateForm = (): boolean => {
        if (formData.password.length < 8) {
            setError('Password must be at least 8 characters');
            return false;
        }
        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match');
            return false;
        }
        return true;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (!validateForm()) return;

        setIsLoading(true);

        try {
            await register({
                email: formData.email,
                password: formData.password,
                firstName: formData.firstName,
                lastName: formData.lastName,
                phone: formData.phone || undefined,
                address: formData.address || undefined,
                dateOfBirth: formData.dateOfBirth || undefined,
            });
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Registration failed');
        } finally {
            setIsLoading(false);
        }
    };

    const PasswordToggleIcon = ({ show }: { show: boolean }) => (
        show ? (
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
            </svg>
        ) : (
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
            </svg>
        )
    );

    return (
        <div className="auth-page">
            <div className="auth-container auth-container-wide">
                <div className="auth-card">
                    <button className="back-to-home" onClick={() => navigate('/')}>
                        Back to Home
                    </button>

                    <div className="auth-header">
                        <h1>Create Account</h1>
                        <p>Join us to book your trips</p>
                    </div>

                    <form onSubmit={handleSubmit} className="auth-form">
                        {error && <div className="auth-error">{error}</div>}

                        <div className="form-row">
                            <div className="form-group">
                                <label htmlFor="firstName">First Name *</label>
                                <input
                                    id="firstName"
                                    name="firstName"
                                    type="text"
                                    value={formData.firstName}
                                    onChange={handleChange}
                                    placeholder="John"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="lastName">Last Name *</label>
                                <input
                                    id="lastName"
                                    name="lastName"
                                    type="text"
                                    value={formData.lastName}
                                    onChange={handleChange}
                                    placeholder="Doe"
                                    required
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label htmlFor="email">Email *</label>
                            <input
                                id="email"
                                name="email"
                                type="email"
                                value={formData.email}
                                onChange={handleChange}
                                placeholder="john.doe@example.com"
                                required
                                autoComplete="email"
                            />
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label htmlFor="password">Password *</label>
                                <div className="password-input-wrapper">
                                    <input
                                        id="password"
                                        name="password"
                                        type={showPassword ? 'text' : 'password'}
                                        value={formData.password}
                                        onChange={handleChange}
                                        placeholder="Min. 8 characters"
                                        required
                                        minLength={8}
                                        autoComplete="new-password"
                                    />
                                    <button
                                        type="button"
                                        className="password-toggle"
                                        onClick={() => setShowPassword(!showPassword)}
                                    >
                                        <PasswordToggleIcon show={showPassword} />
                                    </button>
                                </div>
                            </div>

                            <div className="form-group">
                                <label htmlFor="confirmPassword">Confirm Password *</label>
                                <div className="password-input-wrapper">
                                    <input
                                        id="confirmPassword"
                                        name="confirmPassword"
                                        type={showConfirmPassword ? 'text' : 'password'}
                                        value={formData.confirmPassword}
                                        onChange={handleChange}
                                        placeholder="Repeat password"
                                        required
                                        autoComplete="new-password"
                                    />
                                    <button
                                        type="button"
                                        className="password-toggle"
                                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                    >
                                        <PasswordToggleIcon show={showConfirmPassword} />
                                    </button>
                                </div>
                            </div>
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label htmlFor="phone">Phone</label>
                                <input
                                    id="phone"
                                    name="phone"
                                    type="tel"
                                    value={formData.phone}
                                    onChange={handleChange}
                                    placeholder="+40 123 456 789"
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="dateOfBirth">Date of Birth</label>
                                <input
                                    id="dateOfBirth"
                                    name="dateOfBirth"
                                    type="date"
                                    value={formData.dateOfBirth}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label htmlFor="address">Address</label>
                            <input
                                id="address"
                                name="address"
                                type="text"
                                value={formData.address}
                                onChange={handleChange}
                                placeholder="Street, City, Country"
                            />
                        </div>

                        <button
                            type="submit"
                            className="auth-submit-btn"
                            disabled={isLoading}
                        >
                            {isLoading ? 'Creating Account...' : 'Create Account'}
                        </button>
                    </form>

                    <div className="auth-footer">
                        <p>
                            Already have an account?{' '}
                            <Link to="/login" className="auth-link">
                                Sign in
                            </Link>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}
