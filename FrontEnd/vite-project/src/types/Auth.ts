export type UserType = 'ADMIN' | 'USER';

export interface User {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    phone?: string;
    address?: string;
    dateOfBirth?: string;
    userType: UserType;
    createdAt: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    phone?: string;
    address?: string;
    dateOfBirth?: string;
}

export interface AuthResponse {
    token: string;
    email: string;
    firstName: string;
    lastName: string;
    phone?: string;
    address?: string;
    dateOfBirth?: string;
    userType: UserType;
}

export interface AuthState {
    user: AuthResponse | null;
    token: string | null;
    isAuthenticated: boolean;
    isLoading: boolean;
}
