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

    getTimetableById: baseTimetableApi.getById,

    createTimetable: baseTimetableApi.create,

    updateTimetable: baseTimetableApi.update,
};

