import type { AuthResponse, LoginRequest, RegisterRequest, User } from '../types/Auth';
import { API_CONFIG } from '../config/api';

const AUTH_URL = `${API_CONFIG.BASE_URL}/api/auth`;

function getAuthHeader(): HeadersInit {
    const token = localStorage.getItem('auth_token');
    return token ? { Authorization: `Bearer ${token}` } : {};
}

async function handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
        const error = await response.json().catch(() => ({ message: 'An error occurred' }));
        throw new Error(error.message || 'Request failed');
    }
    return response.json();
}

export const authApi = {
    async login(credentials: LoginRequest): Promise<AuthResponse> {
        const response = await fetch(`${AUTH_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(credentials),
        });
        return handleResponse<AuthResponse>(response);
    },

    async register(data: RegisterRequest): Promise<AuthResponse> {
        const response = await fetch(`${AUTH_URL}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
        return handleResponse<AuthResponse>(response);
    },

    async getCurrentUser(): Promise<User> {
        const response = await fetch(`${AUTH_URL}/me`, {
            headers: {
                'Content-Type': 'application/json',
                ...getAuthHeader(),
            },
        });
        return handleResponse<User>(response);
    },
};

export function getAuthHeaders(): HeadersInit {
    return {
        'Content-Type': 'application/json',
        ...getAuthHeader(),
    };
}
