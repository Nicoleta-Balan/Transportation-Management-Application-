import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import type { AuthResponse, AuthState, LoginRequest, RegisterRequest, User } from '../types/Auth';
import { authApi } from '../services/authApi';

interface AuthContextType extends AuthState {
    login: (credentials: LoginRequest) => Promise<void>;
    register: (data: RegisterRequest) => Promise<void>;
    logout: () => void;
    refreshUser: () => Promise<User | null>;
    isTokenExpired: () => boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

// Helper function to decode JWT and check expiration
function parseJwt(token: string): { exp?: number } | null {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );
        return JSON.parse(jsonPayload);
    } catch {
        return null;
    }
}

function isJwtExpired(token: string): boolean {
    const payload = parseJwt(token);
    if (!payload || !payload.exp) return true;
    // exp is in seconds, Date.now() is in milliseconds
    // Add 10 second buffer to avoid edge cases
    return Date.now() >= (payload.exp * 1000) - 10000;
}

export function AuthProvider({ children }: { children: ReactNode }) {
    const [state, setState] = useState<AuthState>({
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: true,
    });

    // Check if current token is expired
    const isTokenExpired = useCallback(() => {
        const token = localStorage.getItem(TOKEN_KEY);
        if (!token) return true;
        return isJwtExpired(token);
    }, []);

    const logout = useCallback(() => {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);

        setState({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
        });
    }, []);

    // Initialize auth state from localStorage
    useEffect(() => {
        const token = localStorage.getItem(TOKEN_KEY);
        const userStr = localStorage.getItem(USER_KEY);

        if (token && userStr) {
            // Check if token is expired
            if (isJwtExpired(token)) {
                localStorage.removeItem(TOKEN_KEY);
                localStorage.removeItem(USER_KEY);
                setState(prev => ({ ...prev, isLoading: false }));
                return;
            }

            try {
                const user = JSON.parse(userStr) as AuthResponse;
                setState({
                    user,
                    token,
                    isAuthenticated: true,
                    isLoading: false,
                });
            } catch {
                localStorage.removeItem(TOKEN_KEY);
                localStorage.removeItem(USER_KEY);
                setState(prev => ({ ...prev, isLoading: false }));
            }
        } else {
            setState(prev => ({ ...prev, isLoading: false }));
        }
    }, []);

    // Periodically check if token is expired (every minute)
    useEffect(() => {
        if (!state.isAuthenticated || !state.token) return;

        const checkExpiration = () => {
            if (isJwtExpired(state.token!)) {
                logout();
            }
        };

        const interval = setInterval(checkExpiration, 60000); // Check every minute
        return () => clearInterval(interval);
    }, [state.isAuthenticated, state.token, logout]);

    const login = useCallback(async (credentials: LoginRequest) => {
        const response = await authApi.login(credentials);

        localStorage.setItem(TOKEN_KEY, response.token);
        localStorage.setItem(USER_KEY, JSON.stringify(response));

        setState({
            user: response,
            token: response.token,
            isAuthenticated: true,
            isLoading: false,
        });
    }, []);

    const register = useCallback(async (data: RegisterRequest) => {
        const response = await authApi.register(data);

        localStorage.setItem(TOKEN_KEY, response.token);
        localStorage.setItem(USER_KEY, JSON.stringify(response));

        setState({
            user: response,
            token: response.token,
            isAuthenticated: true,
            isLoading: false,
        });
    }, []);

    const refreshUser = useCallback(async () => {
        if (!state.token) return null;

        try {
            const user = await authApi.getCurrentUser();

            // Update state and localStorage with the refreshed user data
            const updatedUser: AuthResponse = {
                token: state.token,
                email: user.email,
                firstName: user.firstName,
                lastName: user.lastName,
                phone: user.phone,
                address: user.address,
                dateOfBirth: user.dateOfBirth,
                userType: user.userType,
            };

            localStorage.setItem(USER_KEY, JSON.stringify(updatedUser));
            setState(prev => ({
                ...prev,
                user: updatedUser,
            }));

            return user;
        } catch {
            logout();
            return null;
        }
    }, [state.token, logout]);

    return (
        <AuthContext.Provider value={{ ...state, login, register, logout, refreshUser, isTokenExpired }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}
