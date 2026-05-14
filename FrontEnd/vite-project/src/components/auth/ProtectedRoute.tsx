import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import type { UserType } from '../../types/Auth';

interface ProtectedRouteProps {
    children: React.ReactNode;
    requiredRole?: UserType;
}

export default function ProtectedRoute({ children, requiredRole }: ProtectedRouteProps) {
    const { isAuthenticated, isLoading, user } = useAuth();
    const location = useLocation();

    if (isLoading) {
        return (
            <div style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: '100vh',
                fontFamily: 'Helvetica Neue, Helvetica, Arial, sans-serif',
                color: '#666'
            }}>
                Loading...
            </div>
        );
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    if (requiredRole && user?.userType !== requiredRole) {
        if (user?.userType === 'ADMIN') {
            return <Navigate to="/admin" replace />;
        }
        return <Navigate to="/dashboard" replace />;
    }

    return <>{children}</>;
}
