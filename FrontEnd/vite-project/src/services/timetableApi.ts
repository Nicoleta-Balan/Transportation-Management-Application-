import type { Timetable, CreateTimetableRequest, UpdateTimetableRequest } from '../types/Timetable';
import { createResourceApi } from '../utils/createResourceApi';
import { API_CONFIG } from '../config/api';
import { fetchWithErrorHandling } from '../utils/apiClient';

// Create base API with standard CRUD operations
const baseTimetableApi = createResourceApi<Timetable, CreateTimetableRequest, UpdateTimetableRequest>({
    resourceName: 'timetables',
    legacyMethods: {
        delete: 'deleteTimetable',
    },
});

// Override getAll() for timetables (route-scoped resource)
const getAll = (): Promise<Timetable[]> => {
    // This should not be called for timetables (they're route-scoped)
    throw new Error('Timetables are route-scoped. Use getAllForRoute instead.');
};

// Add timetable-specific methods
export const timetableApi = {
    ...baseTimetableApi,
    getAll, // Override with route-scoped version

    async getAllForRoute(routeId: number): Promise<Timetable[]> {
        return fetchWithErrorHandling<Timetable[]>(`${API_CONFIG.BASE_URL}/api/timetables/route/${routeId}`);
    },

    async getTimetablesByRouteId(routeId: number): Promise<Timetable[]> {
        return timetableApi.getAllForRoute(routeId);
    },

    async searchTimetables(fromStationId: number, toStationId: number, date: string): Promise<Timetable[]> {
        const params = new URLSearchParams({
            fromStationId: fromStationId.toString(),
            toStationId: toStationId.toString(),
            date: date
        });
        // Updated URL to match the new /search-api endpoint (bypassing /api prefix)
        // Note: API_CONFIG.BASE_URL usually includes the host (e.g., http://localhost:8085)
        // We need to construct the URL carefully.
        // Assuming API_CONFIG.BASE_URL is just the host, we append /search-api
        // If it includes /api, we need to strip it or use a different config.
        
        // Let's assume BASE_URL is http://localhost:8085 based on previous context
        // We construct the full URL manually to be safe
        const baseUrl = API_CONFIG.BASE_URL.replace(/\/api$/, ''); // Strip trailing /api if present
        return fetchWithErrorHandling<Timetable[]>(`${baseUrl}/search-api/timetables?${params.toString()}`);
    },

    getTimetableById: baseTimetableApi.getById,

    createTimetable: baseTimetableApi.create,

    updateTimetable: baseTimetableApi.update,
};
