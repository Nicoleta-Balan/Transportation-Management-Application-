import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import type { AuthResponse, AuthState, LoginRequest, RegisterRequest, User } from '../types/Auth';
import { authApi } from '../services/authApi';

interface AuthContextType extends AuthState {
    login: (credentials: LoginRequest) => Promise<void>;
    register: (data: RegisterRequest) => Promise<void>;
    logout: () => void;
    refreshUser: () => Promise<User | null>;
}

const AuthContext = createContext<AuthContextType | null>(null);

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

export function AuthProvider({ children }: { children: ReactNode }) {
    const [state, setState] = useState<AuthState>({
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: true,
    });

    // Initialize auth state from localStorage
    useEffect(() => {
        const token = localStorage.getItem(TOKEN_KEY);
        const userStr = localStorage.getItem(USER_KEY);

        if (token && userStr) {
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

    const refreshUser = useCallback(async () => {
        if (!state.token) return null;

        try {
            const user = await authApi.getCurrentUser();
            return user;
        } catch {
            logout();
            return null;
        }
    }, [state.token, logout]);

    return (
        <AuthContext.Provider value={{ ...state, login, register, logout, refreshUser }}>
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
