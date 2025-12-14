import { handleApiError } from './apiErrorHandler';

export async function fetchWithErrorHandling<T>(url: string, options?: RequestInit): Promise<T> {
    const response = await fetch(url, options);
    if (!response.ok) {
        throw await handleApiError(response);
    }
    return response.json();
}

export async function fetchWithoutJson(url: string, options?: RequestInit): Promise<void> {
    const response = await fetch(url, options);
    if (!response.ok) {
        throw await handleApiError(response);
    }
}

