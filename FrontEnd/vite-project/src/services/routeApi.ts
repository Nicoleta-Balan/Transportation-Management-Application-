import type { Route, CreateRouteRequest, UpdateRouteRequest } from '../types/Route';
import { createResourceApi } from '../utils/createResourceApi';
import { fetchWithErrorHandling } from '../utils/apiClient';
import { API_CONFIG } from '../config/api';

// Create base API with standard CRUD operations
const baseRouteApi = createResourceApi<Route, CreateRouteRequest, UpdateRouteRequest>({
    resourceName: 'routes',
    legacyMethods: {
        getAll: 'getAllRoutes',
        delete: 'deleteRoute',
    },
});

// Add route-specific methods with legacy names
export const routeApi = {
    ...baseRouteApi,
    
    // Override getAll() to use search endpoint that eagerly loads routeStops
    // Spring Data REST's default GET /api/routes doesn't load routeStops (LAZY fetch)
    // Use /api/routes/search/allWithStops to get routes with stops loaded
    async getAll(): Promise<Route[]> {
        const response = await fetchWithErrorHandling<Route[]>(
            `${API_CONFIG.BASE_URL}/api/routes/search/allWithStops`
        );
        return Array.isArray(response) ? response : [];
    },
    
    // Override getById() to use search endpoint that eagerly loads routeStops
    // Spring Data REST's default GET /api/routes/{id} doesn't load routeStops (LAZY fetch)
    // Use /api/routes/search/findByIdWithStops?id={id} to get route with stops loaded
    async getById(id: number): Promise<Route> {
        return fetchWithErrorHandling<Route>(
            `${API_CONFIG.BASE_URL}/api/routes/search/findByIdWithStops?id=${id}`
        );
    },
    
    getRouteById: function(id: number) {
        return this.getById(id);
    },
    createRoute: baseRouteApi.create,
    updateRoute: baseRouteApi.update,
};
