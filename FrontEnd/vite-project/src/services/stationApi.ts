import type { Station, CreateStationRequest, UpdateStationRequest } from '../types/Station';

import { API_CONFIG } from '../config/api';

import { handleApiError } from '../utils/apiErrorHandler';

async function fetchWithErrorHandling<T>(url: string, options?: RequestInit): Promise<T> {
    const response = await fetch(url, options);
    if (!response.ok) {
        throw await handleApiError(response);
    }
    return response.json();
}

async function fetchWithoutJson(url: string, options?: RequestInit): Promise<void> {
    const response = await fetch(url, options);
    if (!response.ok) {
        throw await handleApiError(response);
    }
}

export const stationApi = {
    async getAllStations(): Promise<Station[]> {
        return fetchWithErrorHandling<Station[]>(`${API_CONFIG.BASE_URL}/api/stations`);
    },

    async createStation(request: CreateStationRequest): Promise<Station> {
        return fetchWithErrorHandling<Station>(`${API_CONFIG.BASE_URL}/api/stations`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request),
        });
    },

    async updateStation(id: number, request: UpdateStationRequest): Promise<Station> {
        return fetchWithErrorHandling<Station>(`${API_CONFIG.BASE_URL}/api/stations/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request),
        });
    },

    async deleteStation(id: number): Promise<void> {
        return fetchWithoutJson(`${API_CONFIG.BASE_URL}/api/stations/${id}`, {
            method: 'DELETE',
        });
    }
};