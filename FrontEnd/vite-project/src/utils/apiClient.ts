import { handleApiError } from './apiErrorHandler';

function getAuthHeader(): Record<string, string> {
    const token = localStorage.getItem('auth_token');
    return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function fetchWithErrorHandling<T>(url: string, options?: RequestInit): Promise<T> {
    const headers: Record<string, string> = {
        ...getAuthHeader(),
        ...(options?.headers as Record<string, string> || {}),
    };

    const response = await fetch(url, { ...options, headers });
    if (!response.ok) {
        throw await handleApiError(response);
    }
    return response.json();
}

export async function fetchWithoutJson(url: string, options?: RequestInit): Promise<void> {
    const headers: Record<string, string> = {
        ...getAuthHeader(),
        ...(options?.headers as Record<string, string> || {}),
    };

    const response = await fetch(url, { ...options, headers });
    if (!response.ok) {
        throw await handleApiError(response);
    }
}
